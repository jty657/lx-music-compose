package com.lxmusic.mobile.ui.screens.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lxmusic.mobile.LxApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingViewModel : ViewModel() {

    val themeMode: StateFlow<String> = LxApp.app.settings.theme.let { flow ->
        MutableStateFlow("system").also { state ->
            viewModelScope.launch { flow.collect { state.value = it } }
        }
    }

    val quality: StateFlow<String> = LxApp.app.settings.quality.let { flow ->
        MutableStateFlow("128k").also { state ->
            viewModelScope.launch { flow.collect { state.value = it } }
        }
    }

    val blurEnabled: StateFlow<Boolean> = LxApp.app.settings.blurEnabled.let { flow ->
        MutableStateFlow(true).also { state ->
            viewModelScope.launch { flow.collect { state.value = it } }
        }
    }

    val blurRadius: StateFlow<Float> = LxApp.app.settings.blurRadius.let { flow ->
        MutableStateFlow(24f).also { state ->
            viewModelScope.launch { flow.collect { state.value = it } }
        }
    }

    val userApiLog: StateFlow<Boolean> = LxApp.app.settings.enableUserApiLog.let { flow ->
        MutableStateFlow(false).also { state ->
            viewModelScope.launch { flow.collect { state.value = it } }
        }
    }

    fun cycleTheme() {
        viewModelScope.launch {
            val current = themeMode.value
            val next = when (current) {
                "system" -> "light"
                "light" -> "dark"
                "dark" -> "monet"
                else -> "system"
            }
            LxApp.app.settings.setTheme(next)
        }
    }

    fun cycleQuality() {
        viewModelScope.launch {
            val current = quality.value
            val options = listOf("128k", "192k", "320k", "flac", "flac24bit")
            val next = options[(options.indexOf(current) + 1).coerceAtMost(options.size - 1)]
            LxApp.app.settings.setQuality(next)
        }
    }

    fun setBlurEnabled(enabled: Boolean) {
        viewModelScope.launch { LxApp.app.settings.setBlurEnabled(enabled) }
    }

    fun setBlurRadius(radius: Float) {
        viewModelScope.launch { LxApp.app.settings.setBlurRadius(radius) }
    }

    fun setUserApiLog(enabled: Boolean) {
        viewModelScope.launch { LxApp.app.settings.setEnableUserApiLog(enabled) }
    }
}
