package com.lingxu.music.core.repository

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 预置音源仓库：把 assets/scripts/presets 下的音源脚本在首次启动时导入到本地数据库。
 * 用户仍可在「音源管理」里删除或改用网络/文件导入。
 */
class PresetScriptRepository(
    private val context: Context,
    private val apiSourceRepository: ApiSourceRepository,
) {
    companion object {
        private const val TAG = "PresetScript"
        private const val PRESET_DIR = "scripts/presets"
        private const val META_PRESET_IMPORTED = "preset_imported_v1"
    }

    /** 导入预置音源；已导入过则跳过 */
    suspend fun importPresetsIfNeeded() {
        val prefs = context.getSharedPreferences("preset", Context.MODE_PRIVATE)
        if (prefs.getBoolean(META_PRESET_IMPORTED, false)) return
        withContext(Dispatchers.IO) {
            runCatching {
                val names = context.assets.list(PRESET_DIR) ?: emptyArray()
                names.filter { it.endsWith(".js") }.forEach { name ->
                    val script = context.assets.open("$PRESET_DIR/$name").use { it.readBytes().toString(Charsets.UTF_8) }
                    runCatching { apiSourceRepository.importScript(script) }
                        .onFailure { Log.w(TAG, "import preset $name failed: ${it.message}") }
                }
            }.onFailure { Log.w(TAG, "list presets failed: ${it.message}") }
        }
        prefs.edit().putBoolean(META_PRESET_IMPORTED, true).apply()
    }
}
