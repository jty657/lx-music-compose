package com.lingxu.music.ui.screens.source

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingxu.music.LxApp
import com.lingxu.music.core.model.ApiSourceInfo
import com.lingxu.music.core.repository.ApiSourceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class SourceViewModel : ViewModel() {

    private val repo: ApiSourceRepository = LxApp.app.apiSourceRepository

    val userApis: StateFlow<List<ApiSourceInfo>> = repo.list.let { flow ->
        MutableStateFlow(emptyList<ApiSourceInfo>()).also { state ->
            viewModelScope.launch {
                flow.collect { state.value = it }
            }
        }
    }

    var currentSource by mutableStateOf("kw")
        private set

    var statusMessage by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            currentSource = LxApp.app.settings.currentApiSource()
        }
    }

    fun switchSource(apiId: String) {
        currentSource = apiId
        LxApp.app.setApiSource(apiId)
        statusMessage = if (apiId.startsWith("user_api")) "正在初始化音源…" else null
        if (apiId.startsWith("user_api")) {
            viewModelScope.launch {
                // 等初始化真正出结果（awaitInit 内部有超时）；注意不能只等 message 非空——
                // loadScript 会先写 ApiStatus(false, "initing")，那样会立刻误报失败。
                val ok = LxApp.app.engineManager.awaitInit()
                statusMessage = if (ok) null else "音源初始化失败：${LxApp.app.engineManager.status.value.message}"
            }
        }
    }

    /** 网络导入（对应原版 ScriptImportOnline） */
    fun importFromUrl(url: String) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            statusMessage = "无效的 URL"
            return
        }
        viewModelScope.launch {
            statusMessage = "正在下载脚本…"
            runCatching {
                withContext(Dispatchers.IO) {
                    val client = OkHttpClient()
                    val request = Request.Builder().url(url).build()
                    client.newCall(request).execute().use { resp ->
                        if (!resp.isSuccessful) throw IllegalStateException("HTTP ${resp.code}")
                        val body = resp.body?.string() ?: throw IllegalStateException("empty body")
                        if (body.length > ApiSourceRepository.MAX_SCRIPT_SIZE) {
                            throw IllegalStateException("脚本过大（超过 9MB）")
                        }
                        body
                    }
                }
            }.onSuccess { script ->
                runCatching {
                    if (repo.count() >= ApiSourceRepository.MAX_COUNT) {
                        throw IllegalStateException("最多只能添加 20 个自定义音源")
                    }
                    repo.importScript(script)
                }.onSuccess { info ->
                    statusMessage = "导入成功：${info.name}"
                }.onFailure { e ->
                    statusMessage = "导入失败：${e.message}"
                }
            }.onFailure { e ->
                statusMessage = "下载失败：${e.message}"
            }
        }
    }

    /** 文件导入（对应原版 ScriptImportExport） */
    fun importFromFile(uri: Uri) {
        viewModelScope.launch {
            statusMessage = "正在读取文件…"
            runCatching {
                withContext(Dispatchers.IO) {
                    val resolver = LxApp.app.contentResolver
                    resolver.openInputStream(uri)?.use { input ->
                        val script = input.readBytes().toString(Charsets.UTF_8)
                        if (script.length > ApiSourceRepository.MAX_SCRIPT_SIZE) {
                            throw IllegalStateException("脚本过大（超过 9MB）")
                        }
                        script
                    } ?: throw IllegalStateException("读取文件失败")
                }
            }.onSuccess { script ->
                runCatching {
                    if (repo.count() >= ApiSourceRepository.MAX_COUNT) {
                        throw IllegalStateException("最多只能添加 20 个自定义音源")
                    }
                    repo.importScript(script)
                }.onSuccess { info ->
                    statusMessage = "导入成功：${info.name}"
                }.onFailure { e ->
                    statusMessage = "导入失败：${e.message}"
                }
            }.onFailure { e ->
                statusMessage = "读取文件失败：${e.message}"
            }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            repo.remove(listOf(id))
            if (currentSource == id) {
                switchSource("kw")
            }
            statusMessage = "已删除"
        }
    }
}
