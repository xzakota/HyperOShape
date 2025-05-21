package com.xzakota.oshape.startup.rule.systemui.api

import com.xzakota.android.hook.startup.base.BaseReflectObject
import com.xzakota.code.extension.getObjectField

class KeyguardClockInjector(instance: Any) : BaseReflectObject(instance) {
    val keyguardClockView by lazy {
        instance.getObjectField("mKeyguardClockView")
    }
}
