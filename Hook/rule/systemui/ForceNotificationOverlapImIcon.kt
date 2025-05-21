package com.xzakota.oshape.startup.rule.systemui

import android.app.Notification
import android.content.pm.ApplicationInfo
import com.xzakota.android.hook.extension.replaceMethod
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.forceOverlapImIconPackages
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.miuiBaseNotifUtil

object ForceNotificationOverlapImIcon : BaseHook() {
    @Suppress("DEPRECATION")
    override fun onHookLoad() {
        miuiBaseNotifUtil.replaceMethod("getNotificationTypeForIm", Notification::class.java) { param ->
            val notification = param.argAs<Notification>()
            if (notification.extras.getCharSequence("hyperOs.category")?.isNotEmpty() == true) {
                return@replaceMethod 0
            }

            val applicationInfo = notification.extras.getParcelable<ApplicationInfo>("android.appInfo")
            if (applicationInfo != null) {
                val packageName = applicationInfo.packageName
                val channelId = notification.channelId
                val hasLargeIcon = notification.largeIcon != null || notification.getLargeIcon() != null

                if ("com.tencent.mm" == packageName && channelId.startsWith("message_channel") && hasLargeIcon) {
                    return@replaceMethod 1
                }

                if (forceOverlapImIconPackages.contains(packageName) && hasLargeIcon) {
                    return@replaceMethod 1
                }
            }

            return@replaceMethod -1
        }
    }
}
