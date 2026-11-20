package com.lingxu.music.core.system

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

/**
 * 电池优化白名单工具（对齐原版 utils 模块的「忽略电池优化 / REQUEST_IGNORE_BATTERY_OPTIMIZATIONS」能力）。
 *
 * 原版通过 REQUEST_IGNORE_BATTERY_OPTIMIZATIONS 权限把自身加入电池优化白名单，
 * 避免 Doze 代际导致后台播放被系统冻结。Android 无法由 App 直接把自己加入白名单，
 * 只能拉起系统设置页由用户确认，这里封装查询状态与跳转。
 */
object BatteryOptimization {

    /** 当前是否已在电池优化白名单中（即已忽略优化）。 */
    fun isIgnoring(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return false
        return runCatching { pm.isIgnoringBatteryOptimizations(context.packageName) }
            .getOrDefault(false)
    }

    /** 拉起「忽略电池优化」系统授权页；返回是否成功发出 Intent。 */
    fun requestIgnore(context: Context): Boolean = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } else {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
        true
    }.getOrDefault(false)
}
