package com.xzakota.oshape.startup.rule.android

import com.xzakota.android.hook.extension.replaceMethodByName
import com.xzakota.oshape.startup.base.BaseHook

object NotificationRightIconSize : BaseHook() {
    override fun onHookLoad() {
        val iconSize = prefs.getInt("prefs_key_android_notification_right_icon_size", 48)
        if (iconSize == 0) {
            loadClass("android.app.Notification\$Builder").replaceMethodByName("calculateRightIconDimens")
        } else {
            resHooker.replaceDimen("android", "notification_right_icon_size", iconSize)
        }
    }
}
