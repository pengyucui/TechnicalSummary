package com.example.lib_xinge_push

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.util.Log
import com.okeyun.util.NoticeServiceEvent
import org.greenrobot.eventbus.EventBus

/**
 * 重新不生效解决办法
 * https://www.zhihu.com/question/33540416
 * https://www.jianshu.com/p/981e7de2c7be
 * 使用
 * https://www.jianshu.com/p/981e7de2c7be
 *
 */
class NotificationMonitorService : NotificationListenerService() {

    // 在收到消息时触发
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // TODO Auto-generated method stub
        val extras = sbn.getNotification().extras
        // 获取接收消息APP的包名
        val notificationPkg = sbn.getPackageName()
        // 获取接收消息的抬头
        val notificationTitle = extras.getString(Notification.EXTRA_TITLE)
        // 获取接收消息的内容
        val notificationText = extras.getString(Notification.EXTRA_TEXT)
        Log.i(
            "XSL_Test",
            "Notification posted $notificationTitle & $notificationText & $notificationPkg"
        )
        if (!TextUtils.equals("com.qlife.owner", notificationPkg)) {
            return
        }
        if (notificationTitle.isNullOrEmpty()) {
            return
        }
        if (notificationText.isNullOrEmpty()) {
            return
        }
        EventBus.getDefault().post(NoticeServiceEvent())
    }

    // 在删除消息时触发
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // TODO Auto-generated method stub
        val extras = sbn.getNotification().extras
        // 获取接收消息APP的包名
        val notificationPkg = sbn.getPackageName()
        // 获取接收消息的抬头
        val notificationTitle = extras.getString(Notification.EXTRA_TITLE)
        // 获取接收消息的内容
        val notificationText = extras.getString(Notification.EXTRA_TEXT)
        Log.i("XSL_Test", "Notification removed $notificationTitle & $notificationText")
    }

}