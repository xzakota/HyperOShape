package com.xzakota.oshape.startup.rule.systemui

import android.content.res.Configuration
import android.view.VelocityTracker
import com.xzakota.android.hook.extension.beforeHookedMethodByName
import com.xzakota.android.hook.extension.replaceMethodByName
import com.xzakota.code.extension.getFloatField
import com.xzakota.code.extension.getObjectField
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.systemui.shared.Keyguard.keyguardMoveRightController

object CustomizeKeyguardGesture : BaseHook() {
    override fun onHookLoad() {
        val keyguardMoveHelper = loadClass("com.android.keyguard.KeyguardMoveHelper")
        keyguardMoveHelper.beforeHookedMethodByName("setTranslation") { param ->
            val currentScreen = param.nonNullThisObject.getObjectField("mCurrentScreen")
            if (currentScreen != Configuration.ORIENTATION_PORTRAIT) {
                return@beforeHookedMethodByName
            }

            val translation = param.argAs<Float>()
            if (slideRight == 1 && translation > 0F) {
                param.args[0] = 0F
            }

            if (slideLeft == 1 && translation > 0F) {
                // param.args[0] = 0F
            }
        }

        keyguardMoveHelper.beforeHookedMethodByName("endMotion") { param ->
            val helper = param.nonNullThisObject
            val currentScreen = helper.getObjectField("mCurrentScreen")
            if (currentScreen != Configuration.ORIENTATION_PORTRAIT || slideLeft != 1) {
                return@beforeHookedMethodByName
            }

            val t = helper.getFloatField("mTranslation")
            val tracker = helper.getObjectFieldAs<VelocityTracker?>("mVelocityTracker")
            val xVelocity = if (tracker == null) {
                0F
            } else {
                tracker.computeCurrentVelocity(1000)
                tracker.xVelocity
            }

            if (xVelocity * t < 0.01f) {
                param.result = null
            }
        }

        if (slideRight == 1) {
            keyguardMoveRightController.replaceMethodByName("onTouchDown", null)
            keyguardMoveRightController.replaceMethodByName("onTouchMove", true)
        }
    }

    // 左滑
    private val slideLeft by lazy {
        prefs.getStringAsInt("systemui_lockscreen_slide_left")
    }

    // 右滑
    private val slideRight by lazy {
        prefs.getStringAsInt("systemui_lockscreen_slide_right")
    }
}
