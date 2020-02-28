## 一、华为厂商通道遇到的问题

   需要在华为官网配置`SHA256`

   在华为官网发送推送时不能使用信鸽返回的通用token，需要使用     `otherPushToken`否则会报无效`token`。日志如下：

```
I/XINGE: [a] Already binder other push succeed token with accid = 2100351696  token = aa4c1d40a47b45715a9712cc15097944ed04ece9 otherPushType = huawei otherPushToken = 0862915037121726300005525500CN01
```

## 二、点击推送跳转的问题

不能在继承`XGPushBaseReceiver`的`onNotifactionClickedResult`中处理跳转，否则华为会跳转到指定页面后重启App。需要使用scheme方式跳转。

## 三、需求收到通知刷新列表

官网`onNotifactionShowedResult`是可以检测到非华为通道通知（小米未测）。

可以使用`NotificationListenerService`去监测。但需要开启通知使用权限。非允许通知权限。