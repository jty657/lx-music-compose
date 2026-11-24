package com.lingxu.music.ui.screens.source

import android.content.ClipboardManager
import android.content.Context
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

    /**
     * 网络导入是否进行中（对齐原版 ScriptImportOnline 下载期间禁用确认键）。
     * 防止连点「导入」重复下载/重复入库；UI 据此禁用按钮并改文案为「导入中…」。
     */
    var importing by mutableStateOf(false)
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
        if (importing) return
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            setStatus("无效的 URL", isError = true)
            return
        }
        importing = true
        viewModelScope.launch {
            // 对齐原版 ImportBtn：达到上限立即拦截，不做无谓的网络下载
            if (repo.count() >= ApiSourceRepository.MAX_COUNT) {
                setStatus("最多只能添加 ${ApiSourceRepository.MAX_COUNT} 个自定义音源", isError = true)
                importing = false
                return@launch
            }
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
            importing = false
        }
    }

    /** 文件导入（对应原版 ScriptImportExport） */
    fun importFromFile(uri: Uri) {
        viewModelScope.launch {
            // 对齐原版 ImportBtn：达到上限立即拦截，不做无谓的文件读取
            if (repo.count() >= ApiSourceRepository.MAX_COUNT) {
                setStatus("最多只能添加 ${ApiSourceRepository.MAX_COUNT} 个自定义音源", isError = true)
                return@launch
            }
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

    /**
     * 剪贴板导入（对齐 LX Music 手机版「复制音源链接/脚本后一键导入」习惯）。
     * 剪贴板内容以 http(s) 开头时按网络 URL 处理，否则按脚本文本处理；
     * 落库统一走 [commitScript]，与文件/网络导入共享数量与大小校验。
     */
    fun importFromClipboard() {
        val text = runCatching {
            val cm = LxApp.app.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            cm?.primaryClip?.getItemAt(0)?.text?.toString()?.trim()
        }.getOrNull()
        if (text.isNullOrEmpty()) {
            setStatus("剪贴板中没有可导入的内容", isError = true)
            return
        }
        if (text.startsWith("http://") || text.startsWith("https://")) {
            importFromUrl(text)
        } else {
            commitScript(text)
        }
    }

    /** 脚本落库：大小/数量校验 + 导入，供剪贴板导入复用（与文件/网络导入同规则） */
    private fun commitScript(script: String) {
        if (script.length > ApiSourceRepository.MAX_SCRIPT_SIZE) {
            setStatus("脚本过大（超过 9MB）", isError = true)
            return
        }
        viewModelScope.launch {
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
        }
    }

    /** 切换单个音源的「更新提示」开关（原版 List.tsx 的 update alert 开关） */
    fun setAllowShowUpdateAlert(id: String, enable: Boolean) {
        viewModelScope.launch {
            repo.setAllowShowUpdateAlert(id, enable)
        }
    }

    /**
     * 导出自定义音源脚本（对齐原版 ScriptImportExport 的导出能力）。
     *
     * [uri] 由调用方通过 SAF CreateDocument 取得，这里只负责把已落库的脚本文本写出去：
     * 复用仓库既有的 [ApiSourceRepository.getScript]，不新增数据源、不改动数据结构；
     * 全部 IO 走 Dispatchers.IO，结果只更新状态提示，不触碰播放/列表状态。
     */
    fun exportScript(id: String, uri: Uri) {
        viewModelScope.launch {
            setStatus("正在导出音源…")
            runCatching {
                withContext(Dispatchers.IO) {
                    val script = repo.getScript(id)
                        ?: throw IllegalStateException("脚本文本不存在")
                    val out = LxApp.app.contentResolver.openOutputStream(uri)
                        ?: throw IllegalStateException("无法写入目标文件")
                    out.use { it.write(script.toByteArray(Charsets.UTF_8)) }
                }
            }.onSuccess {
                setStatus("导出成功")
            }.onFailure { e ->
                setStatus("导出失败：${e.message}", isError = true)
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
