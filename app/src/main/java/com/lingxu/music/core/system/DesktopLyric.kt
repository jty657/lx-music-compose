package com.lingxu.music.core.system

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import com.lingxu.music.LxApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * 桌面歌词（悬浮窗）—— 对齐原版 lx-music-mobile 的 `desktopLyric.*` 能力。
 *
 * 原版用 RN 的浮层窗口把当前歌词贴在其它应用之上；移植版此前完全没有这条链路，
 * 用户切到微信/浏览器后只能靠通知栏判断在放什么。这里用最朴素的
 * `WindowManager` + 原生 View 实现，**不引入任何依赖**，也不接入 Compose：
 *
 * - 悬浮窗是独立 Window，没有 Lifecycle/ViewTreeOwner，放 `ComposeView` 需要额外
 *   挂 `ViewTreeLifecycleOwner`/`SavedStateRegistryOwner`，属于「为样式引入新风险」；
 * - 原生 `TextView` 的开销远小于一个 Compose 组合树，且窗口每 500ms 随播放进度刷新时
 *   不会触发任何 Compose 重组。
 *
 * ### 生命周期与边界
 * - 只在用户显式开启（设置项 `desktopLyric.enable`）且已授予「悬浮窗权限」时创建；
 * - 窗口挂在本进程上，进程存活期间常驻（应用有前台播放服务，正常听歌时进程不会被回收）；
 *   进程被系统回收后悬浮窗消失，下次打开 App 会按开关自动恢复；
 * - 所有窗口操作都 `runCatching` 兜底：权限在使用中被用户撤销、或个别 ROM 拒绝
 *   `TYPE_APPLICATION_OVERLAY` 时只记录日志，绝不让播放流程崩溃。
 *
 * ### 交互（对齐手机版习惯）
 * - 上下拖动移动位置，松手记住位置；
 * - 轻点切换播放/暂停（与原版悬浮条点按语义一致）；
 * - 暂停时降低不透明度，恢复播放时淡入，动效只动 alpha，不触发布局。
 */
object DesktopLyric {

    private const val PREFS = "desktop_lyric"
    private const val KEY_Y = "windowY"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var job: Job? = null
    private var windowManager: WindowManager? = null
    private var container: LinearLayout? = null
    private var mainText: TextView? = null
    private var subText: TextView? = null
    private var params: WindowManager.LayoutParams? = null

    /** 屏幕高度（px），用于把窗口钳制在可视区域内 */
    private var screenHeightPx: Int = 0
    private var accentColor: Int = Color.WHITE
    /** 当前行颜色覆盖（原版 desktopLyric.style.lyricPlayedColor）；null = 跟随主题主色 */
    private var playedColorOverride: Int? = null
    /** 后续行（未播放）颜色覆盖（原版 desktopLyric.style.lyricUnplayColor）；null = 历史次要白 */
    private var unplayColorOverride: Int? = null
    /** 后续行默认色：70% 白，比当前行暗一档以体现视觉层级（历史行为） */
    private val defaultUnplayColor: Int = 0xB3FFFFFF.toInt()
    private var fontSizeSp: Float = 18f
    /** 位置是否锁定（对齐原版 desktopLyric.isLock）：锁定时禁止拖动，轻点暂停/继续仍可用 */
    private var locked: Boolean = false
    /** 单行显示（对齐原版 desktopLyric.isSingleLine）：开启后隐藏翻译副行 */
    private var singleLine: Boolean = false
    /** 暂停/播放切换动画（对齐原版 desktopLyric.showToggleAnima） */
    private var toggleAnima: Boolean = true
    /** 文字不透明度 10~100（对齐原版 desktopLyric.style.opacity） */
    private var textOpacity: Int = 100
    /** 文字水平对齐（对齐原版 desktopLyric.textPosition.x）：left / center / right */
    private var textGravity: Int = Gravity.CENTER
    /** 悬浮歌词显示行数（对齐原版 desktopLyric.maxLineNum）：1~8，默认 5。单行模式下强制为 1 */
    private var maxLineNum: Int = 5
    /** 悬浮窗宽度占屏幕的百分比（对齐原版 desktopLyric.width）：10~100，默认 100 */
    private var widthPercent: Int = 100
    /** 上一次刷新用的内容指纹：只有歌词行/播放状态变化才改 TextView，避免每 500ms 白改一次 */
    private var lastKey: String? = null

    var isShowing: Boolean = false
        private set

    /** 是否已获得悬浮窗权限（API 23 以下默认有权限） */
    fun canShow(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            runCatching { Settings.canDrawOverlays(context) }.getOrDefault(false)
        } else {
            true
        }

    /**
     * 主题主色（用于歌词前的 ♪ 记号）。
     * 悬浮窗拿不到 Compose 的 CompositionLocal，由 AppRoot 在主题变化时推过来。
     */
    fun setAccentColor(color: Int) {
        if (accentColor == color) return
        accentColor = color
        // 只重绘当前行，不重建窗口
        lastKey = null
        runCatching { LxApp.app.playerRepository.state.value }.getOrNull()?.let { refresh(it) }
    }

    /**
     * 当前行歌词颜色（原版 desktopLyric.style.lyricPlayedColor）。
     * hex 为 #RRGGBB；传 null 回到「跟随主题主色」。只重绘文本，
     * 不重建窗口、不触碰播放内核；解析失败时保持原色（不把非法值推给原生层）。
     */
    fun setPlayedColor(hex: String?) {
        val parsed = hex?.let { runCatching { Color.parseColor(it) }.getOrNull() }
        if (playedColorOverride == parsed) return
        playedColorOverride = parsed
        lastKey = null
        runCatching { LxApp.app.playerRepository.state.value }.getOrNull()?.let { refresh(it) }
    }

    /**
     * 后续行（未播放）歌词颜色（原版 desktopLyric.style.lyricUnplayColor）。
     * hex 为 #RRGGBB；传 null 回到历史「次要白」。只重绘文本，
     * 不重建窗口、不触碰播放内核；解析失败时保持原色。
     */
    fun setUnplayColor(hex: String?) {
        val parsed = hex?.let { runCatching { Color.parseColor(it) }.getOrNull() }
        if (unplayColorOverride == parsed) return
        unplayColorOverride = parsed
        lastKey = null
        runCatching { LxApp.app.playerRepository.state.value }.getOrNull()?.let { refresh(it) }
    }

    /** 字号（sp）：设置页滑块实时调整，窗口已存在时立即生效 */
    fun setFontSize(sp: Float) {
        val clamped = sp.coerceIn(12f, 30f)
        if (fontSizeSp == clamped) return
        fontSizeSp = clamped
        mainText?.setTextSize(TypedValue.COMPLEX_UNIT_SP, clamped)
        subText?.setTextSize(TypedValue.COMPLEX_UNIT_SP, clamped * 0.72f)
    }

    /**
     * 位置锁定开关（对齐原版 desktopLyric.isLock）。仅影响拖动：
     * 锁定后手指按下不再移动窗口，但轻点仍能切换播放/暂停，与原版悬浮条语义一致。
     * 只改内存布尔值，不重建窗口、不触碰播放内核。
     */
    fun setLocked(value: Boolean) {
        locked = value
    }

    /**
     * 单行显示（对齐原版 desktopLyric.isSingleLine）。开启后翻译副行始终隐藏。
     * 直接改可见性即可，不动窗口；重置指纹让下次同内容刷新也重新应用可见性。
     */
    fun setSingleLine(value: Boolean) {
        if (singleLine == value) return
        singleLine = value
        mainText?.maxLines = if (value) 1 else maxLineNum
        if (value) subText?.visibility = android.view.View.GONE
        lastKey = null
        runCatching { LxApp.app.playerRepository.state.value }.getOrNull()?.let { refresh(it) }
    }

    /**
     * 暂停/播放切换动画（对齐原版 desktopLyric.showToggleAnima）。
     * 关闭时 [refresh] 直接设 alpha，不走补间；只影响透明度过渡，不影响文本刷新。
     */
    fun setToggleAnima(value: Boolean) {
        toggleAnima = value
    }

    /**
     * 文字不透明度 10~100（对齐原版 desktopLyric.style.opacity）。
     * 采用「整窗 alpha」实现：播放态 alpha = opacity/100，暂停态再乘 0.55，
     * 与原版「文字淡」观感一致且零额外绘制层（报告记录为简化实现）。
     */
    fun setTextOpacity(value: Int) {
        val clamped = value.coerceIn(10, 100)
        if (textOpacity == clamped) return
        textOpacity = clamped
        lastKey = null
        runCatching { LxApp.app.playerRepository.state.value }.getOrNull()?.let { refresh(it) }
    }

    /**
     * 文字水平对齐（对齐原版 desktopLyric.textPosition.x）。
     * 取 left/center/right 映射到 Gravity，其它值回退 center；直接改两个 TextView 的 gravity。
     */
    fun setTextPositionX(value: String) {
        val gravity = when (value) {
            "left" -> Gravity.START
            "right" -> Gravity.END
            else -> Gravity.CENTER
        }
        if (textGravity == gravity) return
        textGravity = gravity
        mainText?.gravity = gravity
        subText?.gravity = gravity
    }

    /**
     * 悬浮歌词显示行数（对齐原版 desktopLyric.maxLineNum，1~8，默认 5）。
     * 只改内存值与主 TextView 的 maxLines，不重建窗口；重置指纹让下次同内容刷新也按新行数重排。
     * 单行模式（[singleLine]）开启时主文本强制 1 行，行数设置暂不生效。
     */
    fun setMaxLineNum(value: Int) {
        val clamped = value.coerceIn(1, 8)
        if (maxLineNum == clamped) return
        maxLineNum = clamped
        mainText?.maxLines = if (singleLine) 1 else clamped
        lastKey = null
        runCatching { LxApp.app.playerRepository.state.value }.getOrNull()?.let { refresh(it) }
    }

    /**
     * 悬浮窗宽度百分比（对齐原版 desktopLyric.width，10~100，默认 100）。
     *
     * 窗口已存在时按新百分比重算宽度并 `updateViewLayout`——只改 width、保留当前 x/y 位置，
     * 避免调宽度时歌词跳回初始位置。不重建窗口、不触碰播放内核，
     * 最坏情况（updateViewLayout 失败）保持原宽度。
     */
    fun setWidthPercent(value: Int) {
        val clamped = value.coerceIn(10, 100)
        if (widthPercent == clamped) return
        widthPercent = clamped
        val view = container ?: return
        val current = params ?: return
        val wm = windowManager ?: return
        current.width = computeWidth(view.context)
        runCatching { wm.updateViewLayout(view, current) }
    }

    /**
     * 按当前 [widthPercent] 计算悬浮窗宽度（px）。start 与 setWidthPercent 共用，保证两处一致：
     * 100% 保留「屏幕宽 - 24dp」内边距观感，低于 100% 按占比计算，最小 160dp 保证可读。
     */
    private fun computeWidth(context: android.content.Context): Int {
        val density = context.resources.displayMetrics.density
        fun dp(value: Int): Int = (value * density).toInt()
        val screenW = context.resources.displayMetrics.widthPixels
        return if (widthPercent >= 100) {
            (screenW - dp(24)).coerceAtLeast(dp(160))
        } else {
            (screenW * widthPercent / 100f).toInt().coerceAtLeast(dp(160))
        }
    }

    /**
     * 显示悬浮歌词。调用前应确保 [canShow] 为 true；
     * 已显示时重复调用是安全的空操作。
     */
    fun start(context: Context) {
        if (isShowing) return
        if (!canShow(context)) return
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return
        val density = context.resources.displayMetrics.density
        fun dp(value: Int): Int = (value * density).toInt()

        screenHeightPx = context.resources.displayMetrics.heightPixels
        // 悬浮窗宽度占屏幕百分比（原版 desktopLyric.width，默认 100）
        val width = computeWidth(context)

        val main = TextView(context).apply {
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp)
            // 单行模式强制 1 行，否则按 maxLineNum（原版默认 5）显示当前行起的连续歌词
            maxLines = if (singleLine) 1 else maxLineNum
            ellipsize = TextUtils.TruncateAt.END
            gravity = textGravity
            includeFontPadding = false
            typeface = Typeface.DEFAULT_BOLD
            // 悬浮在任意背景之上：描边阴影保证浅色壁纸下仍可读
            setShadowLayer(6f, 0f, 1f, 0xB3000000.toInt())
        }
        val sub = TextView(context).apply {
            setTextColor(0xCCFFFFFF.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp * 0.72f)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            gravity = textGravity
            includeFontPadding = false
            setShadowLayer(5f, 0f, 1f, 0x99000000.toInt())
            visibility = android.view.View.GONE
        }
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(14), dp(8), dp(14), dp(9))
            background = GradientDrawable().apply {
                cornerRadius = dp(16).toFloat()
                setColor(0xB3000000.toInt())
                setStroke(dp(1), 0x26FFFFFF)
            }
        }
        root.addView(main, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        root.addView(sub, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val lp = WindowManager.LayoutParams(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            // NOT_FOCUSABLE：不抢输入焦点，不弹输入法；
            // NOT_TOUCH_MODAL：窗口外的触摸照常交给下层应用
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(12)
            y = prefs.getInt(KEY_Y, screenHeightPx * 11 / 16)
        }

        // 拖动移动 + 轻点播放/暂停。用原始坐标差计算，避免窗口自身位移带来的抖动。
        var downRawY = 0f
        var startY = 0
        var downAt = 0L
        var dragged = false
        root.setOnTouchListener { view, event ->
            val current = params
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downRawY = event.rawY
                    startY = current?.y ?: 0
                    downAt = SystemClock.uptimeMillis()
                    dragged = false
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    // 锁定时不移动窗口（对齐原版 isLock）；dragged 保持 false，
                    // 抬手时仍可按「轻点」语义切换播放/暂停，只是位置不会改变。
                    if (!locked) {
                        val delta = (event.rawY - downRawY).toInt()
                        if (!dragged && abs(delta) > dp(4)) dragged = true
                        if (dragged && current != null) {
                            val maxY = (screenHeightPx - view.height).coerceAtLeast(0)
                            current.y = (startY + delta).coerceIn(0, maxY)
                            runCatching { windowManager?.updateViewLayout(view, current) }
                        }
                    }
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (!dragged && SystemClock.uptimeMillis() - downAt < 300) {
                        runCatching { LxApp.app.playerRepository.togglePlay() }
                    } else if (current != null) {
                        prefs.edit().putInt(KEY_Y, current.y).apply()
                    }
                    true
                }

                else -> false
            }
        }

        val added = runCatching { wm.addView(root, lp) }.isSuccess
        if (!added) return
        isShowing = true

        windowManager = wm
        container = root
        mainText = main
        subText = sub
        params = lp
        lastKey = null

        // 订阅播放状态：只在「歌词行/播放状态」变化时刷新文本，播放进度本身不驱动 UI
        job = scope.launch {
            runCatching {
                LxApp.app.playerRepository.state.collect { refresh(it) }
            }
        }
        runCatching { LxApp.app.playerRepository.state.value }.getOrNull()?.let { refresh(it) }
    }

    /** 关闭悬浮歌词并释放窗口 */
    fun stop() {
        job?.cancel()
        job = null
        val view = container
        if (view != null) runCatching { windowManager?.removeView(view) }
        container = null
        mainText = null
        subText = null
        params = null
        windowManager = null
        lastKey = null
        isShowing = false
    }

    /** 按当前播放状态刷新文本（幂等：内容未变时直接返回） */
    private fun refresh(state: com.lingxu.music.core.player.PlayState) {
        val main = mainText ?: return
        val sub = subText ?: return
        val index = state.currentLyricIndex
        val lines = state.lyricLines
        val line = lines.getOrNull(index)
        // 指纹纳入样式状态：改单行/行数/透明度/对齐后，即便歌词与播放态未变也要重算文本与 alpha
        val key = "${state.current?.id.orEmpty()}|$index|${lines.size}|${state.isPlaying}|$accentColor|$playedColorOverride|$unplayColorOverride|$singleLine|$maxLineNum|$textOpacity|$textGravity"
        if (key == lastKey) return
        lastKey = key

        // 当前行颜色：优先用户所选色板，未设置时跟随主题主色（老用户升级零突变）
        val currentColor = playedColorOverride ?: accentColor
        // 单行模式强制 1 行，否则按 maxLineNum 显示当前行起的连续歌词（对齐原版 maxLineNum）
        val effectiveLines = if (singleLine) 1 else maxLineNum
        val sb = StringBuilder()
        // 后续行需要着次要色的区间，构建文本时同步记录，避免事后重算与拼接结果错位
        val subRanges = mutableListOf<Pair<Int, Int>>()
        var firstRange: Pair<Int, Int>? = null
        if (index >= 0 && lines.isNotEmpty()) {
            for (i in 0 until effectiveLines) {
                val row = lines.getOrNull(index + i) ?: break
                if (row.text.isBlank()) continue
                if (sb.isNotEmpty()) sb.append('\n')
                val start = sb.length
                // ♪ 与当前行整体同色，与原版 playedColor 语义一致
                if (firstRange == null) sb.append("♪ ")
                sb.append(row.text)
                if (firstRange == null) firstRange = start to sb.length
                else subRanges.add(start to sb.length)
            }
        }
        if (sb.isEmpty()) {
            main.text = SpannableString("♪ 暂无歌词").apply {
                setSpan(
                    ForegroundColorSpan(currentColor),
                    0,
                    1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
            sub.visibility = android.view.View.GONE
        } else {
            val text = SpannableString(sb.toString())
            // 当前行的「♪」用主题主色标记（对应原版已播放色）；后续行默认用次要白色体现层级，
            // 用户显式选色后（原版 lyricUnplayColor）整体按所选色渲染。
            val unplayColor = unplayColorOverride ?: defaultUnplayColor
            subRanges.forEach { (start, end) ->
                text.setSpan(
                    ForegroundColorSpan(unplayColor),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
            // 默认仅 ♪ 用主题色（与升级前完全一致）；显式选色后整行按 playedColor 渲染，
            // 更贴近原版语义。后续行始终保持次要白色，层级关系不变。
            text.setSpan(ForegroundColorSpan(currentColor), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            playedColorOverride?.let { color ->
                firstRange?.let { (start, end) ->
                    text.setSpan(ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
            main.text = text
            val translation = line?.translation?.takeIf { it.isNotBlank() }
            // 单行模式或没有翻译时隐藏副行（对齐原版 isSingleLine）
            if (singleLine || translation == null) {
                sub.visibility = android.view.View.GONE
            } else {
                sub.text = translation
                sub.visibility = android.view.View.VISIBLE
            }
        }

        // 目标不透明度 = 文字不透明度 × 播放态系数（播放 1.0 / 暂停 0.55）。
        // 暂停降透明度不额外加图标，避免遮挡下层内容。
        val target = (textOpacity / 100f) * if (state.isPlaying) 1f else 0.55f
        val root = container ?: return
        // areAnimatorsEnabled 是 API 26 才有的：minSdk 24，必须门控后再调用。
        // 系统「移除动画」或用户关闭切换动画时直接设 alpha（静帧，省逐帧调度，符合可访问性）。
        val animatorsEnabled = toggleAnima &&
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || ValueAnimator.areAnimatorsEnabled())
        if (!animatorsEnabled) {
            root.alpha = target
        } else if (root.alpha != target) {
            root.animate().alpha(target).setDuration(180L).start()
        }
    }
}
