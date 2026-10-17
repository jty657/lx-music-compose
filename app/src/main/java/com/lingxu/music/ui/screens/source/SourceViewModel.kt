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
import kotlinx.coroutines.flow.first
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

    /** 状态提示是失败还是成功/进行中（此前一律按 error 色渲染，导入成功也显示成红字） */
    var statusIsError by mutableStateOf(false)
        private set

    private fun setStatus(message: String?, isError: Boolean = false) {
        statusMessage = message
        statusIsError = isError
    }

    init {
        viewModelScope.launch {
            currentSource = LxApp.app.settings.currentApiSource()
        }
    }

    fun switchSource(apiId: String) {
        currentSource = apiId
        LxApp.app.setApiSource(apiId)
        setStatus(if (apiId.startsWith("user_api")) "正在初始化音源…" else null)
        if (apiId.startsWith("user_api")) {
            viewModelScope.launch {
                // 注意不能在这里直接 awaitInit()：setApiSource 只是往 Main 队列里派发，此时
                // engineManager 还是「上一个音源」的状态（内置源时 currentApiId==null → 直接返回 true）。
                // 改为等状态离开「initing」并给出最终结果。
                val result = LxApp.app.engineManager.status.first { s ->
                    when {
                        s.status -> true
                        s.message == "initing" -> false
                        else -> s.message.isNotEmpty()
                    }
                }
                setStatus(if (result.status) null else "音源初始化失败：${result.message}", isError = !result.status)
            }
        }
    }

    /** 网络导入（对应原版 ScriptImportOnline） */
    fun importFromUrl(url: String) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            setStatus("无效的 URL", isError = true)
            return
        }
        viewModelScope.launch {
            setStatus("正在下载脚本…")
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
                    setStatus("导入成功：${info.name}")
                }.onFailure { e ->
                    setStatus("导入失败：${e.message}", isError = true)
                }
            }.onFailure { e ->
                setStatus("下载失败：${e.message}", isError = true)
            }
        }
    }

    /** 文件导入（对应原版 ScriptImportExport） */
    fun importFromFile(uri: Uri) {
        viewModelScope.launch {
            setStatus("正在读取文件…")
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
                    setStatus("导入成功：${info.name}")
                }.onFailure { e ->
                    setStatus("导入失败：${e.message}", isError = true)
                }
            }.onFailure { e ->
                setStatus("读取文件失败：${e.message}", isError = true)
            }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            repo.remove(listOf(id))
            if (currentSource == id) {
                switchSource("kw")
            }
            setStatus("已删除")
        }
    }
}
