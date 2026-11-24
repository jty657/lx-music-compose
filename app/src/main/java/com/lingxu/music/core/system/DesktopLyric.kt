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
    private var fontSizeSp: Float = 18f
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

    /** 字号（sp）：设置页滑块实时调整，窗口已存在时立即生效 */
    fun setFontSize(sp: Float) {
        val clamped = sp.coerceIn(12f, 30f)
        if (fontSizeSp == clamped) return
        fontSizeSp = clamped
        mainText?.setTextSize(TypedValue.COMPLEX_UNIT_SP, clamped)
        subText?.setTextSize(TypedValue.COMPLEX_UNIT_SP, clamped * 0.72f)
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
        val width = (context.resources.displayMetrics.widthPixels - dp(24)).coerceAtLeast(dp(160))

        val main = TextView(context).apply {
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            gravity = Gravity.CENTER
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
            gravity = Gravity.CENTER
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
                    val delta = (event.rawY - downRawY).toInt()
                    if (!dragged && abs(delta) > dp(4)) dragged = true
                    if (dragged && current != null) {
                        val maxY = (screenHeightPx - view.height).coerceAtLeast(0)
                        current.y = (startY + delta).coerceIn(0, maxY)
                        runCatching { windowManager?.updateViewLayout(view, current) }
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
        val line = state.lyricLines.getOrNull(index)
        val key = "${state.current?.id.orEmpty()}|$index|${state.lyricLines.size}|${state.isPlaying}|$accentColor"
        if (key == lastKey) return
        lastKey = key

        if (line == null || line.text.isBlank()) {
            main.text = SpannableString("♪ 暂无歌词").apply {
                setSpan(
                    ForegroundColorSpan(accentColor),
                    0,
                    1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
            sub.visibility = android.view.View.GONE
        } else {
            // 「♪」用主题主色标记当前正在唱的行，其余文字保持白色（对应原版已播放色/未播放色）
            val text = SpannableString("♪ ${line.text}")
            text.setSpan(ForegroundColorSpan(accentColor), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            main.text = text
            val translation = line.translation?.takeIf { it.isNotBlank() }
            if (translation == null) {
                sub.visibility = android.view.View.GONE
            } else {
                sub.text = translation
                sub.visibility = android.view.View.VISIBLE
            }
        }

        // 暂停时整体降到 55% 不透明度：不额外加图标，避免遮挡下层内容
        val target = if (state.isPlaying) 1f else 0.55f
        val root = container ?: return
        if (!ValueAnimator.areAnimatorsEnabled()) {
            root.alpha = target
        } else if (root.alpha != target) {
            root.animate().alpha(target).setDuration(180L).start()
        }
    }
}
