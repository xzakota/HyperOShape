package com.xzakota.oshape.startup.rule.home.layout

import com.xzakota.android.hook.extension.beforeHookedMethod
import com.xzakota.android.util.DensityUtils.dp2px
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.home.shared.HomeShared.deviceConfig

abstract class BaseDeviceConfig(
    private val methodName: String,
    valueKey: String,
    defaultValue: Int
) : BaseHook() {
    override fun onHookLoad() {
        deviceConfig.beforeHookedMethod(methodName) { param ->
            param.result = value
        }
    }

    private val value by lazy {
        dp2px(prefs.getInt(valueKey, defaultValue))
    }
}
