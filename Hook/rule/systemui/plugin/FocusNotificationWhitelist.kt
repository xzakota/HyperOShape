package com.xzakota.oshape.startup.rule.systemui.plugin

import android.content.Context
import com.xzakota.android.hook.core.loader.base.IMemberHookParam
import com.xzakota.android.hook.extension.beforeHookedMethod
import com.xzakota.oshape.application.AppConstant
import com.xzakota.oshape.startup.rule.systemui.api.PluginInstance
import com.xzakota.oshape.startup.rule.systemui.api.PluginListener
import com.xzakota.oshape.startup.rule.systemui.shared.Notification

class FocusNotificationWhitelist(
    pluginInstance: PluginInstance,
    listener: PluginListener
) : BasePluginHook(pluginInstance, listener) {
    override fun onHookLoad() {
        val managerClass = loadClass("miui.systemui.notification.NotificationSettingsManager")

        addHook(managerClass.beforeHookedMethod("canShowFocus", Context::class.java, String::class.java) {
            allowShowFocus(it, it.args[1])
        })
        addHook(managerClass.beforeHookedMethod("canCustomFocus", String::class.java) {
            allowShowFocus(it, it.args[0])
        })
    }

    private fun allowShowFocus(param: IMemberHookParam, arg: Any?, isFromPlugin: Boolean = true) {
        if (isFromPlugin) {
            if (Notification.focusNotificationWhitelistPackages.contains(arg)) {
                param.result = true
            }
        } else if (arg == AppConstant.APP_ID) {
            param.result = true
        }
    }
}
