package com.xzakota.oshape.startup.rule.systemui.api

import android.os.Handler
import android.os.Vibrator
import com.xzakota.android.hook.startup.base.BaseReflectObject
import com.xzakota.code.extension.callMethodAs
import com.xzakota.code.extension.callVoidMethod
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.code.extension.getStaticBooleanField
import com.xzakota.code.util.reflect.InvokeUtils.loadClass
import miui.util.HapticFeedbackUtil
import miui.util.HapticFeedbackUtil.EFFECT_ID_BUTTON_LIGHT
import miui.util.HapticFeedbackUtil.EFFECT_KEY_FLICK_LIGHT

class HapticFeedBack(instance: Any) : BaseReflectObject(instance) {
    val vibrator by lazy {
        instance.getObjectFieldAs<Vibrator>("mVibrator")
    }

    val hapticFeedbackUtil by lazy {
        instance.getObjectFieldAs<HapticFeedbackUtil>("mHapticFeedbackUtil")
    }

    fun isSupportExtHapticFeedback(
        effectId: Int
    ): Boolean = instance.callMethodAs("isSupportExtHapticFeedback", effectId)

    fun isSupportV2HapticFeedback(effectId: Int): Boolean = instance.callMethodAs("isSupportV2HapticFeedback", effectId)

    fun extExtHapticFeedback(
        effectId1: Int,
        effectId2: Int,
        key: String,
        milliseconds: Int,
        handler: Handler? = null
    ) = instance.callVoidMethod("extExtHapticFeedback", effectId1, effectId2, key, milliseconds, handler)

    fun postToBgThreadIfNeed(
        handler: Handler? = null,
        runnable: Runnable
    ) = instance.callVoidMethod("postToBgThreadIfNeed", handler, runnable)

    fun clickFeedback() {
        if (!IS_SUPPORT_LINEAR_MOTOR_VIBRATE) {
            return
        }

        postToBgThreadIfNeed {
            if (IS_SUPPORT_V2_HAPTIC_VERSION) {
                hapticFeedbackUtil.performExtHapticFeedback(EFFECT_ID_BUTTON_LIGHT)
            } else {
                hapticFeedbackUtil.performHapticFeedback(EFFECT_KEY_FLICK_LIGHT, false)
            }
        }
    }

    companion object {
        private val I_HAPTIC_FEEDBACK = loadClass("com.miui.interfaces.IHapticFeedBack")

        val IS_SUPPORT_LINEAR_MOTOR_VIBRATE = I_HAPTIC_FEEDBACK.getStaticBooleanField("IS_SUPPORT_LINEAR_MOTOR_VIBRATE")
        val IS_SUPPORT_V2_HAPTIC_VERSION = I_HAPTIC_FEEDBACK.getStaticBooleanField("IS_SUPPORT_V2_HAPTIC_VERSION")
    }
}
