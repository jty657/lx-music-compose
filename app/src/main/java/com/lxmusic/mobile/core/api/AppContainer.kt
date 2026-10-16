package com.lxmusic.mobile.core.api

import com.lxmusic.mobile.core.engine.toOldMusicInfo
import com.lxmusic.mobile.core.model.MusicInfo
import kotlinx.coroutines.flow.StateFlow

/**
 * 音乐获取统一接口。
 * getMusicUrl/getLyric/getPic 的形状与原版 musicSdk[source] 的方法一致。
 */
interface MusicApi {
    /** 获取歌曲播放 URL（原版返回 {promise, canceleFn}，这里简化返回 url 或抛异常） */
    suspend fun getMusicUrl(musicInfo: MusicInfo, type: String): String

    /** 获取歌词 */
    suspend fun getLyric(musicInfo: MusicInfo): com.lxmusic.mobile.core.model.LyricInfo

    /** 获取封面 */
    suspend fun getPic(musicInfo: MusicInfo): String
}

/** 应用级仓库容器 */
class AppContainer(
    val musicRepository: com.lxmusic.mobile.core.repository.MusicRepository,
    val apiSourceRepository: com.lxmusic.mobile.core.repository.ApiSourceRepository,
    val settingsRepository: com.lxmusic.mobile.core.repository.SettingsRepository,
    val playerRepository: com.lxmusic.mobile.core.player.PlayerRepository,
    val engineManager: com.lxmusic.mobile.core.engine.ScriptEngineManager,
) {
    val qualityList: StateFlow<Map<String, List<String>>> = musicRepository.qualityList
}
