package com.lingxu.music

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.lingxu.music.core.player.PlaybackService
import com.lingxu.music.ui.AppRoot

class MainActivity : ComponentActivity() {

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNeededPermissions()
        // 启动播放服务（MediaSession 接管播放，提供后台播放与媒体通知）
        startService(Intent(this, PlaybackService::class.java))
        setContent {
            AppRoot()
        }
    }

    override fun onResume() {
        super.onResume()
        // 定时退出到点：对齐原版 timeoutExit.exit() 的 exitApp 分支——直接退出应用回到桌面，
        // 而不是仅暂停播放。每次回到前台刷新引用，避免持有已销毁的 Activity。
        LxApp.app.playerRepository.onTimeoutExit = { finishAffinity() }
    }

    private fun requestNeededPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }
}
