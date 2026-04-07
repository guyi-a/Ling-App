package com.guyi.demo1.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.util.UUID

/**
 * 设备信息工具类
 */
object DeviceUtils {

    /**
     * 获取设备唯一标识
     * 优先使用 ANDROID_ID，失败则生成 UUID
     */
    fun getDeviceId(context: Context): String {
        return try {
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            ) ?: generateUUID()
        } catch (e: Exception) {
            generateUUID()
        }
    }

    /**
     * 获取设备型号
     * 格式：品牌 型号（如：Google Pixel 6）
     */
    fun getDeviceModel(): String {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            model
        } else {
            "$manufacturer $model"
        }
    }

    /**
     * 生成唯一 UUID
     */
    private fun generateUUID(): String {
        return UUID.randomUUID().toString()
    }
}
