package com.lingxu.music.core.system

import android.content.Context
import android.os.Build
import java.util.Locale

/**
 * 设备信息（对齐原版 utils 模块的「设备信息（设备名、ABI、系统语言）」能力）。
 */
object DeviceInfo {

    fun deviceName(context: Context): String = runCatching {
        android.provider.Settings.Secure.getString(
            context.contentResolver,
            "device_name",
        ) ?: Build.MODEL
    }.getOrDefault(Build.MODEL)

    fun model(): String = "${Build.BRAND} ${Build.MODEL}".trim()

    fun abi(): String = Build.SUPPORTED_ABIS.firstOrNull() ?: Build.ABI

    fun androidVersion(): String = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

    fun language(): String = Locale.getDefault().toString()
}
