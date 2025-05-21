package com.xzakota.oshape.startup.rule.systemui.api

import android.content.Intent
import com.xzakota.android.hook.startup.base.BaseReflectObject
import com.xzakota.code.extension.callVoidMethod

class ActivityStarter(instance: Any) : BaseReflectObject(instance) {
    @JvmOverloads
    fun startActivity(
        intent: Intent,
        isCreateStatusBarTransitionAnimator: Boolean = true
    ) = instance.callVoidMethod("startActivity", intent, isCreateStatusBarTransitionAnimator)

    fun startActivity(
        intent: Intent,
        z: Boolean,
        isCreateStatusBarTransitionAnimator: Boolean
    ) = instance.callVoidMethod("startActivity", intent, z, isCreateStatusBarTransitionAnimator)

    fun postStartActivityDismissingKeyguard(
        intent: Intent,
        i: Int,
        controller: Any? = null
    ) = instance.callVoidMethod("postStartActivityDismissingKeyguard", intent, i, controller)
}
