package com.xzakota.oshape.startup.rule.systemui

import android.view.View
import com.xzakota.android.extension.view.postDelayed
import com.xzakota.android.hook.extension.afterHookedMethodByName
import com.xzakota.android.hook.extension.beforeHookedMethodByName
import com.xzakota.code.extension.callMethodAs
import com.xzakota.code.extension.getBooleanField
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.autoExpandNotificationsPackages
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.expandNotificationRow

object ExpandNotificationRow : BaseHook() {
    override fun onHookLoad() {
        expandNotificationRow.beforeHookedMethodByName("setSystemExpanded") { param ->
            val row = param.nonNullThisObject
            val isOnKeyguard = row.getBooleanField("mOnKeyguard")
            if (!isOnKeyguard) {
                val notification = row.callMethodAs<Any>("getEntry").getObjectFieldAs<Any>("mSbn")
                val packageName = notification.callMethodAs<String>("getPackageName")
                if (autoExpandNotificationsPackages.contains(packageName)) {
                    param.args[0] = true
                }
            }
        }

        expandNotificationRow.afterHookedMethodByName("setHeadsUp") { param ->
            val row = param.thisObjectAs<View>()
            val isShowHeadsUp = param.argAs<Boolean>()
            val isOnKeyguard = row.getBooleanField("mOnKeyguard")
            if (!isOnKeyguard && isShowHeadsUp) {
                val notification = row.callMethodAs<Any>("getEntry").getObjectFieldAs<Any>("mSbn")
                val expandClickListener = row.getObjectFieldAs<View.OnClickListener>("mExpandClickListener")
                val packageName = notification.callMethodAs<String>("getPackageName")
                if (autoExpandNotificationsPackages.contains(packageName)) {
                    row.postDelayed(60) {
                        expandClickListener.onClick(row)
                    }
                }
            }
        }
    }
}
