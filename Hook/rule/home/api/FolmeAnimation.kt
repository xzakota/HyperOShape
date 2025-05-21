package com.xzakota.oshape.startup.rule.home.api

import android.view.View
import com.xzakota.android.hook.startup.base.BaseReflectObject
import com.xzakota.code.extension.createInstance
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.code.util.reflect.InvokeUtils
import miuix.animation.base.AnimConfig
import miuix.animation.base.AnimSpecialConfig
import miuix.animation.controller.AnimState

class FolmeAnimation(instance: Any) : BaseReflectObject(instance) {
    constructor(
        view: View,
        animState: AnimState,
        animConfig: AnimConfig,
        animSpecialConfig: AnimSpecialConfig
    ) : this(FOLME_ANIMATION.createInstance(view, animState.instance, animConfig.instance, animSpecialConfig.instance))

    val animState by lazy {
        AnimState(instance.getObjectFieldAs<Any>("animState"))
    }

    private companion object {
        val FOLME_ANIMATION by lazy {
            InvokeUtils.loadClass("com.miui.home.launcher.anim.FolmeAnimation")
        }
    }
}
