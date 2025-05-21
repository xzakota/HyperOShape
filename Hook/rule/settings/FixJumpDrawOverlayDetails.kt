package com.xzakota.oshape.startup.rule.settings

import android.app.Activity
import android.provider.Settings
import com.xzakota.android.hook.extension.beforeHookedMethodByName
import com.xzakota.code.extension.setStringField
import com.xzakota.oshape.startup.base.BaseHook

object FixJumpDrawOverlayDetails : BaseHook() {
    override fun onHookLoad() {
        loadClass("com.android.settings.SettingsActivity").beforeHookedMethodByName("createUiFromIntent") {
            val activity = it.thisObjectAs<Activity>()
            val intent = activity.intent

            if (Settings.ACTION_MANAGE_OVERLAY_PERMISSION == intent.action && "package" == intent.data?.scheme) {
                activity.setStringField(
                    "initialFragmentName",
                    "com.android.settings.applications.appinfo.DrawOverlayDetails"
                )
            }
        }
    }
}
