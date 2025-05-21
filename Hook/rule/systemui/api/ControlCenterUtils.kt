package com.xzakota.oshape.startup.rule.systemui.api

import android.content.Context
import android.content.Intent
import com.xzakota.code.extension.callStaticMethodAs
import com.xzakota.code.util.reflect.InvokeUtils.loadClass

object ControlCenterUtils {
    private val CONTROL_CENTER_UTILS by lazy {
        loadClass("com.android.systemui.controlcenter.utils.ControlCenterUtils")
    }

    @Deprecated("2025-02-22")
    fun getSettingsSplitIntent(
        context: Context,
        intent: Intent?
    ): Intent = CONTROL_CENTER_UTILS.callStaticMethodAs("getSettingsSplitIntent", context, intent)
}
