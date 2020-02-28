package com.example.lib_xinge_push

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

object NoticeServerHelper {
    /**
     * 检测通知监测权限是否授权
     */
    fun isNotificationListenerEnabled(context: Context): Boolean {
        val packageNames = NotificationManagerCompat.getEnabledListenerPackages(context)
        return packageNames.contains(context.packageName)
    }

    /**
     * 打开通知监测权限
     */
    fun openNotificationListenSettings(context: Context) {
        try {
            val intent: Intent =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                    Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                } else {
                    Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 重新bind 通知监测服务
     */
     fun toggleNotificationListenerService(context: Context) {
        val pm = context.packageManager
        pm.setComponentEnabledSetting(
            ComponentName(context, NotificationMonitorService::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )
        pm.setComponentEnabledSetting(
            ComponentName(context, NotificationMonitorService::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        )
    }
}