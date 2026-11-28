package com.lingxu.music.core.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * 应用设置仓库。key 命名对齐原版 LX.AppSetting（common.* / player.* / playDetail.* / search.* / list.*）
 * 便于与原版配置互相参照与后续同步备份。
 */
class SettingsRepository(private val context: Context) {

    companion object {
        // common
        val KEY_API_SOURCE = stringPreferencesKey("common.apiSource")
        val KEY_THEME = stringPreferencesKey("common.theme")
        // 主题主色（原版 theme.id 的简化对齐）：default/green/blue/purple/orange/pink
        val KEY_THEME_COLOR = stringPreferencesKey("common.themeColor")
        // 原版 theme.dynamicBg：播放页背景跟随封面主色，默认 false（与原版 defaultSetting 一致）
        val KEY_DYNAMIC_BG = booleanPreferencesKey("theme.dynamicBg")
        // 原版 theme.hideBgDark：深色模式下隐藏背景（原版隐藏主题背景大图，移植版是封面主色渐变背景），
        // 默认 false（与原版 defaultSetting 一致）。仅在 dynamicBg 开启时有实际视觉意义。
        val KEY_HIDE_BG_DARK = booleanPreferencesKey("theme.hideBgDark")
        val KEY_LANG = stringPreferencesKey("common.langId")
        val KEY_FONT_SCALE = floatPreferencesKey("common.fontScale")
        val KEY_SHARE_TYPE = stringPreferencesKey("common.shareType")
        val KEY_AUTO_HIDE_PLAY_BAR = booleanPreferencesKey("common.autoHidePlayBar")
        // 原版 common.allowProgressBarSeek：底部迷你条进度条是否可拖动跳转，默认 true
        val KEY_ALLOW_PROGRESS_BAR_SEEK = booleanPreferencesKey("common.allowProgressBarSeek")
        // 原版 common.sourceNameType：音源显示名类型 real（原名）/ alias（别名），默认 real
        val KEY_SOURCE_NAME_TYPE = stringPreferencesKey("common.sourceNameType")
        // 原版 common.showBackBtn：二级页是否显示返回按钮。移植版历史一直显示，
        // 默认 true 保持老用户零突变；与原版默认 false 的差异记录在本轮报告的待确认项。
        val KEY_SHOW_BACK_BTN = booleanPreferencesKey("common.showBackBtn")
        // player
        val KEY_QUALITY = stringPreferencesKey("player.quality")
        val KEY_PLAY_MODE = stringPreferencesKey("player.playMode")
        val KEY_IS_SHOW_LYRIC_TRANSLATION = booleanPreferencesKey("player.isShowLyricTranslation")
        val KEY_IS_SHOW_LYRIC_ROMA = booleanPreferencesKey("player.isShowLyricRoma")
        val KEY_STARTUP_AUTO_PLAY = booleanPreferencesKey("player.startupAutoPlay")
        // 原版 player.startupPushPlayDetailScreen：启动时自动进入播放详情页（默认 false）
        val KEY_STARTUP_PUSH_PLAY_DETAIL = booleanPreferencesKey("player.startupPushPlayDetailScreen")
        val KEY_IS_SAVE_PLAY_TIME = booleanPreferencesKey("player.isSavePlayTime")
        val KEY_IS_HANDLE_AUDIO_FOCUS = booleanPreferencesKey("player.isHandleAudioFocus")
        // 原版 player.isShowNotificationImage：媒体通知/锁屏是否显示专辑封面，默认 true
        val KEY_IS_SHOW_NOTIFICATION_IMAGE = booleanPreferencesKey("player.isShowNotificationImage")
        val KEY_PLAYBACK_RATE = floatPreferencesKey("player.playbackRate")
        val KEY_VOLUME = floatPreferencesKey("player.volume")
        // 原版 player.cacheSize：播放缓存上限，单位 KB（原版存字符串，默认 '1024'）
        val KEY_PLAY_CACHE_SIZE = androidx.datastore.preferences.core.intPreferencesKey("player.cacheSize")
        // 定时退出（对应原版 player.timeoutExit / timeoutExitPlayed）
        val KEY_TIMEOUT_EXIT_MINUTES = androidx.datastore.preferences.core.intPreferencesKey("player.timeoutExit")
        val KEY_TIMEOUT_EXIT_PLAYED = booleanPreferencesKey("player.timeoutExitPlayed")
        // playDetail
        val KEY_LYRIC_ALIGN = stringPreferencesKey("playDetail.style.align")
        val KEY_LYRIC_FONT_SIZE = floatPreferencesKey("playDetail.style.lrcFontSize")
        val KEY_IS_ZOOM_ACTIVE_LRC = booleanPreferencesKey("playDetail.isZoomActiveLrc")
        // 原版 theme.fontShadow：歌词文字加投影，提升浅色封面/壁纸上的可读性
        val KEY_IS_LYRIC_FONT_SHADOW = booleanPreferencesKey("theme.fontShadow")
        // 原版 playDetail.isShowLyricProgressSetting：滚动歌词时显示逐句定位线
        val KEY_SHOW_LYRIC_PROGRESS = booleanPreferencesKey("playDetail.isShowLyricProgressSetting")
        // search
        val KEY_IS_SHOW_HOT_SEARCH = booleanPreferencesKey("search.isShowHotSearch")
        val KEY_IS_SHOW_HISTORY_SEARCH = booleanPreferencesKey("search.isShowHistorySearch")
        // 原版 search.type：记住用户上次选择的搜索类型（音乐/歌单），冷启动恢复
        val KEY_SEARCH_TYPE = stringPreferencesKey("search.type")
        // 原版 search.source：与上同属 getSearchSetting/saveSearchSetting，记住用户上次选择的搜索音源
        val KEY_SEARCH_SOURCE = stringPreferencesKey("search.source")
        // list
        val KEY_IS_SHOW_SOURCE = booleanPreferencesKey("list.isShowSource")
        val KEY_IS_SHOW_ALBUM_NAME = booleanPreferencesKey("list.isShowAlbumName")
        val KEY_IS_SHOW_INTERVAL = booleanPreferencesKey("list.isShowInterval")
        val KEY_ADD_MUSIC_LOCATION = stringPreferencesKey("list.addMusicLocationType")
        // 原版 list.isSaveScrollLocation：返回列表时是否记住离开前的滚动位置，默认 true
        val KEY_SAVE_SCROLL_LOCATION = booleanPreferencesKey("list.isSaveScrollLocation")
        // 原版 list.isClickPlayList：点击列表里的歌曲时切到当前列表播放（仅歌单/榜单），默认开启
        val KEY_IS_CLICK_PLAY_LIST = booleanPreferencesKey("list.isClickPlayList")
        // glass（本项目自有）
        val KEY_BLUR_ENABLED = booleanPreferencesKey("glass.blurEnabled")
        val KEY_BLUR_RADIUS = floatPreferencesKey("glass.blurRadius")
        // desktopLyric（对齐原版 desktopLyric.enable / desktopLyric.style.fontSize）
        val KEY_DESKTOP_LYRIC_ENABLE = booleanPreferencesKey("desktopLyric.enable")
        // 原版 desktopLyric.isLock：锁定悬浮歌词位置（锁定后禁止拖动，轻点暂停/继续仍可用）
        val KEY_DESKTOP_LYRIC_LOCK = booleanPreferencesKey("desktopLyric.isLock")
        // 字号按 sp 存（原版存百分比 100~300），默认 18sp
        val KEY_DESKTOP_LYRIC_FONT_SIZE = floatPreferencesKey("desktopLyric.style.fontSize")
        // 原版 desktopLyric.isSingleLine：单行显示（隐藏翻译副行），默认 false
        val KEY_DESKTOP_LYRIC_SINGLE_LINE = booleanPreferencesKey("desktopLyric.isSingleLine")
        // 原版 desktopLyric.showToggleAnima：暂停/播放切换时的淡入淡出动效，默认 true
        val KEY_DESKTOP_LYRIC_TOGGLE_ANIMA = booleanPreferencesKey("desktopLyric.showToggleAnima")
        // 原版 desktopLyric.style.opacity：文字不透明度 10~100，默认 100
        val KEY_DESKTOP_LYRIC_OPACITY = androidx.datastore.preferences.core.intPreferencesKey("desktopLyric.style.opacity")
        // 原版 desktopLyric.textPosition.x：文字水平对齐 left/center/right
        val KEY_DESKTOP_LYRIC_TEXT_POSITION_X = stringPreferencesKey("desktopLyric.textPosition.x")
        // 原版 desktopLyric.textPosition.y：文字垂直对齐 top/center/bottom
        val KEY_DESKTOP_LYRIC_TEXT_POSITION_Y = stringPreferencesKey("desktopLyric.textPosition.y")
        // 原版 desktopLyric.maxLineNum：悬浮歌词最多显示行数 1~8，默认 5
        val KEY_DESKTOP_LYRIC_MAX_LINE_NUM = androidx.datastore.preferences.core.intPreferencesKey("desktopLyric.maxLineNum")
        // 原版 desktopLyric.width：悬浮窗宽度占屏幕的百分比 10~100，默认 100
        val KEY_DESKTOP_LYRIC_WIDTH = androidx.datastore.preferences.core.intPreferencesKey("desktopLyric.width")
        // 原版 desktopLyric.style.lyricPlayedColor：悬浮歌词当前行主题色（#RRGGBB）。
        // 未设置时沿用移植版历史「跟随主题主色」行为，老用户升级零突变。
        val KEY_DESKTOP_LYRIC_PLAYED_COLOR = stringPreferencesKey("desktopLyric.style.lyricPlayedColor")
        // 原版 desktopLyric.style.lyricUnplayColor：悬浮歌词后续行（未播放）颜色（#RRGGBB）。
        // 未设置时沿用移植版历史「次要白」行为，老用户升级零突变。
        val KEY_DESKTOP_LYRIC_UNPLAY_COLOR = stringPreferencesKey("desktopLyric.style.lyricUnplayColor")
        // 原版 desktopLyric.style.lyricShadowColor：悬浮歌词阴影颜色（#RRGGBB）。
        // 原版色板中 8 档为黑阴影、1 档（黑字）为白阴影；移植版此前按文字亮度自动推导，
        // 未设置时继续沿用自动推导，老用户升级零突变；显式选色则覆盖（对齐原版显式阴影色）。
        val KEY_DESKTOP_LYRIC_SHADOW_COLOR = stringPreferencesKey("desktopLyric.style.lyricShadowColor")
        // userApi
        val KEY_ENABLE_USER_API_LOG = booleanPreferencesKey("userApi.log")
        const val LANG_SYSTEM = "system"
        const val SHARE_SYSTEM = "system"
        const val SOURCE_NAME_REAL = "real"
        const val SOURCE_NAME_ALIAS = "alias"
        const val ALIGN_LEFT = "left"
        const val ADD_LOCATION_TOP = "top"
        const val ADD_LOCATION_BOTTOM = "bottom"
        /** 定时退出：0 表示未开启 */
        const val TIMEOUT_EXIT_OFF = 0
    }

    // ================= common =================

    val apiSource: Flow<String> = context.dataStore.data.map { it[KEY_API_SOURCE] ?: "kw" }
    val theme: Flow<String> = context.dataStore.data.map { it[KEY_THEME] ?: "system" }
    // 默认 default：保持 Miuix 自带配色，升级后观感不变（原版默认为 green，见报告待确认项）
    val themeColor: Flow<String> = context.dataStore.data.map { it[KEY_THEME_COLOR] ?: "default" }
    // 原版 theme.dynamicBg：默认 false，老用户升级后播放页观感零突变
    val dynamicBg: Flow<Boolean> = context.dataStore.data.map { it[KEY_DYNAMIC_BG] ?: false }
    // 原版 theme.hideBgDark：默认 false，深色模式下仍显示背景，老用户观感零突变
    val hideBgDark: Flow<Boolean> = context.dataStore.data.map { it[KEY_HIDE_BG_DARK] ?: false }
    val lang: Flow<String> = context.dataStore.data.map { it[KEY_LANG] ?: LANG_SYSTEM }
    val fontScale: Flow<Float> = context.dataStore.data.map { it[KEY_FONT_SCALE] ?: 1f }
    val shareType: Flow<String> = context.dataStore.data.map { it[KEY_SHARE_TYPE] ?: SHARE_SYSTEM }
    val autoHidePlayBar: Flow<Boolean> = context.dataStore.data.map { it[KEY_AUTO_HIDE_PLAY_BAR] ?: true }
    // 默认 true 与原版 defaultSetting 一致：迷你条进度条可拖动跳转
    val allowProgressBarSeek: Flow<Boolean> = context.dataStore.data.map { it[KEY_ALLOW_PROGRESS_BAR_SEEK] ?: true }
    // 默认 real（原名）：与移植版历史显示一致，老用户升级后观感不变；原版默认 alias，见报告待确认项
    val sourceNameType: Flow<String> = context.dataStore.data.map { it[KEY_SOURCE_NAME_TYPE] ?: SOURCE_NAME_REAL }
    // 默认 true：移植版此前二级页恒有返回键，新增开关后老用户观感零突变；
    // 关闭后仅隐藏图标并禁用点击，48dp 占位保留，避免顶栏标题横向跳动。
    val showBackBtn: Flow<Boolean> = context.dataStore.data.map { it[KEY_SHOW_BACK_BTN] ?: true }

    // ================= player =================

    val quality: Flow<String> = context.dataStore.data.map { it[KEY_QUALITY] ?: "128k" }
    val playMode: Flow<String> = context.dataStore.data.map { it[KEY_PLAY_MODE] ?: "listLoop" }
    // 默认值与原版 defaultSetting（'player.isShowLyricTranslation': false）保持一致
    val showLyricTranslation: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_SHOW_LYRIC_TRANSLATION] ?: false }
    val showLyricRoma: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_SHOW_LYRIC_ROMA] ?: false }
    val startupAutoPlay: Flow<Boolean> = context.dataStore.data.map { it[KEY_STARTUP_AUTO_PLAY] ?: false }
    // 与原版 defaultSetting 的 'player.startupPushPlayDetailScreen': false 保持一致
    val startupPushPlayDetail: Flow<Boolean> = context.dataStore.data.map { it[KEY_STARTUP_PUSH_PLAY_DETAIL] ?: false }
    val isSavePlayTime: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_SAVE_PLAY_TIME] ?: false }
    val isHandleAudioFocus: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_HANDLE_AUDIO_FOCUS] ?: true }
    // 默认 true 与原版 defaultSetting 的 'player.isShowNotificationImage' 一致
    val isShowNotificationImage: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_SHOW_NOTIFICATION_IMAGE] ?: true }
    val playbackRate: Flow<Float> = context.dataStore.data.map { it[KEY_PLAYBACK_RATE] ?: 1f }
    val volume: Flow<Float> = context.dataStore.data.map { it[KEY_VOLUME] ?: 1f }
    // 默认 1024 KB，与原版 defaultSetting 的 'player.cacheSize': '1024' 一致。
    // 老版本（本项不存在）读到默认值，升级后首次启动即启用缓存，与原版行为一致。
    val playCacheSizeKb: Flow<Int> = context.dataStore.data.map {
        runCatching { it[KEY_PLAY_CACHE_SIZE] }.getOrNull() ?: 1024
    }
    val timeoutExitMinutes: Flow<Int> = context.dataStore.data.map { it[KEY_TIMEOUT_EXIT_MINUTES] ?: 0 }
    val timeoutExitPlayed: Flow<Boolean> = context.dataStore.data.map { it[KEY_TIMEOUT_EXIT_PLAYED] ?: true }

    // ================= playDetail =================

    val lyricAlign: Flow<String> = context.dataStore.data.map { it[KEY_LYRIC_ALIGN] ?: ALIGN_LEFT }
    val lyricFontSize: Flow<Float> = context.dataStore.data.map { it[KEY_LYRIC_FONT_SIZE] ?: 1f }
    val isZoomActiveLrc: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_ZOOM_ACTIVE_LRC] ?: false }
    // 默认 false 与原版 defaultSetting 的 'theme.fontShadow': false 一致
    val lyricFontShadow: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_LYRIC_FONT_SHADOW] ?: false }
    // 默认 false 与原版 defaultSetting 一致：手动滚动歌词时是否显示逐句定位线
    val showLyricProgress: Flow<Boolean> = context.dataStore.data.map { it[KEY_SHOW_LYRIC_PROGRESS] ?: false }

    // ================= search =================

    // 默认值与原版 defaultSetting 保持一致：热搜/历史默认关闭（'search.isShowHotSearch': false 等）
    val isShowHotSearch: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_SHOW_HOT_SEARCH] ?: false }
    val isShowHistorySearch: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_SHOW_HISTORY_SEARCH] ?: false }
    // 搜索类型：对齐原版 search.type（"music" / "songlist"），默认音乐
    val searchType: Flow<String> = context.dataStore.data.map { it[KEY_SEARCH_TYPE] ?: "music" }
    val searchSource: Flow<String> = context.dataStore.data.map { it[KEY_SEARCH_SOURCE] ?: "all" }

    // ================= list =================

    val isShowSource: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_SHOW_SOURCE] ?: true }
    val isShowAlbumName: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_SHOW_ALBUM_NAME] ?: false }
    val isShowInterval: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_SHOW_INTERVAL] ?: true }
    // 原版 'list.addMusicLocationType' 默认 'top'（添加到列表顶部）
    val addMusicLocation: Flow<String> = context.dataStore.data.map { it[KEY_ADD_MUSIC_LOCATION] ?: ADD_LOCATION_TOP }
    // 默认 true 与原版 defaultSetting 的 'list.isSaveScrollLocation': true 一致
    val saveScrollLocation: Flow<Boolean> = context.dataStore.data.map { it[KEY_SAVE_SCROLL_LOCATION] ?: true }
    // 原版 list.isClickPlayList：默认 true 保持移植版既有「点歌即切当前列表」行为，老用户零突变
    val isClickPlayList: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_CLICK_PLAY_LIST] ?: true }

    // ================= glass =================

    val blurEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_BLUR_ENABLED] ?: true }
    val blurRadius: Flow<Float> = context.dataStore.data.map { prefs ->
        // 兼容旧版本把 blurRadius 以字符串存放的格式。
        // 注意：DataStore 用 float key 读取同名 string 值会抛 ClassCastException，
        // ?: 捕获不了异常，必须用 runCatching 包住每一步，回退分支才真正生效，
        // 否则升级用户一进设置页就崩。
        runCatching { prefs[KEY_BLUR_RADIUS] }.getOrNull()
            ?: runCatching { prefs[stringPreferencesKey("glass.blurRadius")]?.toFloatOrNull() }.getOrNull()
            ?: 24f
    }

    // ================= userApi =================

    val enableUserApiLog: Flow<Boolean> = context.dataStore.data.map { it[KEY_ENABLE_USER_API_LOG] ?: false }

    // ================= desktopLyric =================

    // 默认 false 与原版 defaultSetting 的 'desktopLyric.enable': false 一致
    val desktopLyricEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_DESKTOP_LYRIC_ENABLE] ?: false }
    // 默认 false 与原版 defaultSetting 的 'desktopLyric.isLock': false 一致
    val desktopLyricLocked: Flow<Boolean> = context.dataStore.data.map { it[KEY_DESKTOP_LYRIC_LOCK] ?: false }
    val desktopLyricFontSize: Flow<Float> = context.dataStore.data.map { it[KEY_DESKTOP_LYRIC_FONT_SIZE] ?: 18f }
    // 下面四项对齐原版 defaultSetting：单行关 / 切换动画开 / 不透明度 100 / 文字对齐。
    // textPosition.x 默认 center 而非原版 left：移植版悬浮窗历史一直是居中，
    // 保持默认可避免老用户升级后观感突变，用户可在设置中切到 left/right（报告已记录该假设）。
    val desktopLyricSingleLine: Flow<Boolean> = context.dataStore.data.map { it[KEY_DESKTOP_LYRIC_SINGLE_LINE] ?: false }
    val desktopLyricToggleAnima: Flow<Boolean> = context.dataStore.data.map { it[KEY_DESKTOP_LYRIC_TOGGLE_ANIMA] ?: true }
    val desktopLyricOpacity: Flow<Int> = context.dataStore.data.map {
        (it[KEY_DESKTOP_LYRIC_OPACITY] ?: 100).coerceIn(10, 100)
    }
    val desktopLyricTextPositionX: Flow<String> = context.dataStore.data.map { it[KEY_DESKTOP_LYRIC_TEXT_POSITION_X] ?: "center" }
    // 默认 top 与原版 defaultSetting 的 'desktopLyric.textPosition.y': 'top' 一致
    val desktopLyricTextPositionY: Flow<String> = context.dataStore.data.map { it[KEY_DESKTOP_LYRIC_TEXT_POSITION_Y] ?: "top" }
    // 原版 desktopLyric.maxLineNum：默认 5 行，容错钳制读到的异常值
    val desktopLyricMaxLineNum: Flow<Int> = context.dataStore.data.map {
        (it[KEY_DESKTOP_LYRIC_MAX_LINE_NUM] ?: 5).coerceIn(1, 8)
    }
    // 原版 desktopLyric.width：默认 100%（铺满），容错钳制读到的异常值
    val desktopLyricWidth: Flow<Int> = context.dataStore.data.map {
        (it[KEY_DESKTOP_LYRIC_WIDTH] ?: 100).coerceIn(10, 100)
    }
    // 原版 desktopLyric.style.lyricPlayedColor：null 表示跟随主题主色（移植版历史行为）。
    // 只接受合法 #RRGGBB，损坏值回退 null，避免把非法色值推给原生层。
    val desktopLyricPlayedColor: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_DESKTOP_LYRIC_PLAYED_COLOR]?.takeIf {
            it.length == 7 && it.startsWith("#") && it.drop(1).all { c -> c.isDigit() || c in 'a'..'f' || c in 'A'..'F' }
        }
    }
    // 原版 desktopLyric.style.lyricUnplayColor：null 表示沿用历史「次要白」（老用户默认）。
    // 与 playedColor 同样的 #RRGGBB 校验，损坏值回退 null，避免把非法色值推给原生层。
    val desktopLyricUnplayColor: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_DESKTOP_LYRIC_UNPLAY_COLOR]?.takeIf {
            it.length == 7 && it.startsWith("#") && it.drop(1).all { c -> c.isDigit() || c in 'a'..'f' || c in 'A'..'F' }
        }
    }
    // 原版 desktopLyric.style.lyricShadowColor：null 表示沿用「按文字亮度自动推导」的历史行为。
    // 与另外两个颜色键同样的 #RRGGBB 校验，损坏值回退 null，避免把非法色值推给原生层。
    val desktopLyricShadowColor: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_DESKTOP_LYRIC_SHADOW_COLOR]?.takeIf {
            it.length == 7 && it.startsWith("#") && it.drop(1).all { c -> c.isDigit() || c in 'a'..'f' || c in 'A'..'F' }
        }
    }

    suspend fun setApiSource(value: String) = context.dataStore.edit { it[KEY_API_SOURCE] = value }
    suspend fun setTheme(value: String) = context.dataStore.edit { it[KEY_THEME] = value }
    suspend fun setThemeColor(value: String) = context.dataStore.edit { it[KEY_THEME_COLOR] = value }
    suspend fun setDynamicBg(value: Boolean) = context.dataStore.edit { it[KEY_DYNAMIC_BG] = value }
    suspend fun setHideBgDark(value: Boolean) = context.dataStore.edit { it[KEY_HIDE_BG_DARK] = value }
    suspend fun setLang(value: String) = context.dataStore.edit { it[KEY_LANG] = value }
    suspend fun setFontScale(value: Float) = context.dataStore.edit { it[KEY_FONT_SCALE] = value }
    suspend fun setShareType(value: String) = context.dataStore.edit { it[KEY_SHARE_TYPE] = value }
    suspend fun setAutoHidePlayBar(value: Boolean) = context.dataStore.edit { it[KEY_AUTO_HIDE_PLAY_BAR] = value }
    suspend fun setAllowProgressBarSeek(value: Boolean) = context.dataStore.edit { it[KEY_ALLOW_PROGRESS_BAR_SEEK] = value }
    suspend fun setSourceNameType(value: String) = context.dataStore.edit { it[KEY_SOURCE_NAME_TYPE] = value }
    suspend fun setShowBackBtn(value: Boolean) = context.dataStore.edit { it[KEY_SHOW_BACK_BTN] = value }

    suspend fun setQuality(value: String) = context.dataStore.edit { it[KEY_QUALITY] = value }
    suspend fun setPlayMode(value: String) = context.dataStore.edit { it[KEY_PLAY_MODE] = value }
    suspend fun setShowLyricTranslation(value: Boolean) = context.dataStore.edit { it[KEY_IS_SHOW_LYRIC_TRANSLATION] = value }
    suspend fun setShowLyricRoma(value: Boolean) = context.dataStore.edit { it[KEY_IS_SHOW_LYRIC_ROMA] = value }
    suspend fun setStartupAutoPlay(value: Boolean) = context.dataStore.edit { it[KEY_STARTUP_AUTO_PLAY] = value }
    suspend fun setStartupPushPlayDetail(value: Boolean) = context.dataStore.edit { it[KEY_STARTUP_PUSH_PLAY_DETAIL] = value }
    suspend fun setIsSavePlayTime(value: Boolean) = context.dataStore.edit { it[KEY_IS_SAVE_PLAY_TIME] = value }
    suspend fun setIsHandleAudioFocus(value: Boolean) = context.dataStore.edit { it[KEY_IS_HANDLE_AUDIO_FOCUS] = value }
    suspend fun setIsShowNotificationImage(value: Boolean) = context.dataStore.edit { it[KEY_IS_SHOW_NOTIFICATION_IMAGE] = value }
    suspend fun setPlaybackRate(value: Float) = context.dataStore.edit { it[KEY_PLAYBACK_RATE] = value }
    suspend fun setVolume(value: Float) = context.dataStore.edit { it[KEY_VOLUME] = value }
    suspend fun setPlayCacheSizeKb(value: Int) = context.dataStore.edit { it[KEY_PLAY_CACHE_SIZE] = value }
    suspend fun setTimeoutExitMinutes(value: Int) = context.dataStore.edit { it[KEY_TIMEOUT_EXIT_MINUTES] = value }
    suspend fun setTimeoutExitPlayed(value: Boolean) = context.dataStore.edit { it[KEY_TIMEOUT_EXIT_PLAYED] = value }

    suspend fun setLyricAlign(value: String) = context.dataStore.edit { it[KEY_LYRIC_ALIGN] = value }
    suspend fun setLyricFontSize(value: Float) = context.dataStore.edit { it[KEY_LYRIC_FONT_SIZE] = value }
    suspend fun setIsZoomActiveLrc(value: Boolean) = context.dataStore.edit { it[KEY_IS_ZOOM_ACTIVE_LRC] = value }
    suspend fun setLyricFontShadow(value: Boolean) = context.dataStore.edit { it[KEY_IS_LYRIC_FONT_SHADOW] = value }
    suspend fun setShowLyricProgress(value: Boolean) = context.dataStore.edit { it[KEY_SHOW_LYRIC_PROGRESS] = value }

    suspend fun setIsShowHotSearch(value: Boolean) = context.dataStore.edit { it[KEY_IS_SHOW_HOT_SEARCH] = value }
    suspend fun setIsShowHistorySearch(value: Boolean) = context.dataStore.edit { it[KEY_IS_SHOW_HISTORY_SEARCH] = value }
    suspend fun setSearchType(value: String) = context.dataStore.edit { it[KEY_SEARCH_TYPE] = value }
    suspend fun setSearchSource(value: String) = context.dataStore.edit { it[KEY_SEARCH_SOURCE] = value }

    suspend fun setIsShowSource(value: Boolean) = context.dataStore.edit { it[KEY_IS_SHOW_SOURCE] = value }
    suspend fun setIsShowAlbumName(value: Boolean) = context.dataStore.edit { it[KEY_IS_SHOW_ALBUM_NAME] = value }
    suspend fun setIsShowInterval(value: Boolean) = context.dataStore.edit { it[KEY_IS_SHOW_INTERVAL] = value }
    suspend fun setAddMusicLocation(value: String) = context.dataStore.edit { it[KEY_ADD_MUSIC_LOCATION] = value }
    suspend fun setSaveScrollLocation(value: Boolean) = context.dataStore.edit { it[KEY_SAVE_SCROLL_LOCATION] = value }
    suspend fun setIsClickPlayList(value: Boolean) = context.dataStore.edit { it[KEY_IS_CLICK_PLAY_LIST] = value }

    suspend fun setBlurEnabled(value: Boolean) = context.dataStore.edit { it[KEY_BLUR_ENABLED] = value }
    suspend fun setBlurRadius(value: Float) = context.dataStore.edit { it[KEY_BLUR_RADIUS] = value }
    suspend fun setEnableUserApiLog(value: Boolean) = context.dataStore.edit { it[KEY_ENABLE_USER_API_LOG] = value }

    suspend fun setDesktopLyricEnabled(value: Boolean) = context.dataStore.edit { it[KEY_DESKTOP_LYRIC_ENABLE] = value }
    suspend fun setDesktopLyricLocked(value: Boolean) = context.dataStore.edit { it[KEY_DESKTOP_LYRIC_LOCK] = value }
    suspend fun setDesktopLyricFontSize(value: Float) = context.dataStore.edit { it[KEY_DESKTOP_LYRIC_FONT_SIZE] = value }
    suspend fun setDesktopLyricSingleLine(value: Boolean) = context.dataStore.edit { it[KEY_DESKTOP_LYRIC_SINGLE_LINE] = value }
    suspend fun setDesktopLyricToggleAnima(value: Boolean) = context.dataStore.edit { it[KEY_DESKTOP_LYRIC_TOGGLE_ANIMA] = value }
    suspend fun setDesktopLyricOpacity(value: Int) = context.dataStore.edit { it[KEY_DESKTOP_LYRIC_OPACITY] = value.coerceIn(10, 100) }
    suspend fun setDesktopLyricTextPositionX(value: String) = context.dataStore.edit { it[KEY_DESKTOP_LYRIC_TEXT_POSITION_X] = value }
    suspend fun setDesktopLyricTextPositionY(value: String) = context.dataStore.edit { it[KEY_DESKTOP_LYRIC_TEXT_POSITION_Y] = value }
    suspend fun setDesktopLyricMaxLineNum(value: Int) = context.dataStore.edit { it[KEY_DESKTOP_LYRIC_MAX_LINE_NUM] = value.coerceIn(1, 8) }
    suspend fun setDesktopLyricWidth(value: Int) = context.dataStore.edit { it[KEY_DESKTOP_LYRIC_WIDTH] = value.coerceIn(10, 100) }
    // 传 null 表示清除覆盖、回到「跟随主题主色」，不写入空字符串
    suspend fun setDesktopLyricPlayedColor(value: String?) = context.dataStore.edit { prefs ->
        if (value == null) prefs.remove(KEY_DESKTOP_LYRIC_PLAYED_COLOR)
        else prefs[KEY_DESKTOP_LYRIC_PLAYED_COLOR] = value
    }
    // 传 null 表示清除覆盖、回到历史「次要白」，不写入空字符串
    suspend fun setDesktopLyricUnplayColor(value: String?) = context.dataStore.edit { prefs ->
        if (value == null) prefs.remove(KEY_DESKTOP_LYRIC_UNPLAY_COLOR)
        else prefs[KEY_DESKTOP_LYRIC_UNPLAY_COLOR] = value
    }
    // 传 null 表示清除覆盖、回到「按文字亮度自动推导」，不写入空字符串
    suspend fun setDesktopLyricShadowColor(value: String?) = context.dataStore.edit { prefs ->
        if (value == null) prefs.remove(KEY_DESKTOP_LYRIC_SHADOW_COLOR)
        else prefs[KEY_DESKTOP_LYRIC_SHADOW_COLOR] = value
    }

    suspend fun currentApiSource(): String = apiSource.first()
    suspend fun currentQuality(): String = quality.first()
    suspend fun currentLang(): String = lang.first()
}
