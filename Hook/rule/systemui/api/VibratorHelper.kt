package com.xzakota.oshape.startup.rule.systemui.api

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import com.xzakota.android.hook.startup.base.BaseReflectObject
import com.xzakota.code.extension.callVoidMethod
import com.xzakota.code.extension.getObjectFieldAs

@SuppressLint("MissingPermission")
class VibratorHelper(instance: Any) : BaseReflectObject(instance) {
    val vibrator by lazy {
        instance.getObjectFieldAs<Vibrator>("mVibrator")
    }

    fun cancel() {
        vibrator.cancel()
    }

    fun vibrate(effect: VibrationEffect) = instance.callVoidMethod("vibrate", effect)
    fun vibrate(effect: VibrationEffect, attr: AudioAttributes) = instance.callVoidMethod("vibrate", effect, attr)

    fun vibrateForLongPress() {
        vibrator.getPrimitiveDurations(*intArrayOf(8, 3))
        val effect = VibrationEffect.startComposition()
            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.5f)
            .compose()

        vibrate(effect)
    }
}
