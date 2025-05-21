package com.xzakota.oshape.startup.rule.systemui.api

import com.xzakota.android.hook.startup.base.BaseReflectObject
import com.xzakota.code.extension.getBooleanField

class KeyguardStateController(instance: Any) : BaseReflectObject(instance) {
    val showing by lazy {
        instance.getBooleanField("mShowing")
    }
}
