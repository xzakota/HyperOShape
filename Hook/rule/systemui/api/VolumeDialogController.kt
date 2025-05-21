package com.xzakota.oshape.startup.rule.systemui.api

import com.xzakota.android.hook.startup.base.BaseReflectObject
import com.xzakota.code.extension.getObjectFieldAs

class VolumeDialogController(instance: Any) : BaseReflectObject(instance) {
    val vibratorHelper by lazy {
        VibratorHelper(instance.getObjectFieldAs("mVibrator"))
    }
}
