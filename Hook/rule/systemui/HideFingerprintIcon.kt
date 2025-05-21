package com.xzakota.oshape.startup.rule.systemui

import android.content.Context
import com.xzakota.android.extension.content.getDrawableIdByName
import com.xzakota.android.hook.extension.beforeHookedMethod
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.oshape.R
import com.xzakota.oshape.startup.base.BaseHook

object HideFingerprintIcon : BaseHook() {
    private var normal: Int? = null
    private var light: Int? = null
    // private var aod: Int? = null

    override fun onHookLoad() {
        loadClass("com.miui.keyguard.biometrics.fod.MiuiGxzwFrameAnimation").beforeHookedMethod(
            "draw", Int::class.java
        ) { param ->
            val anim = param.nonNullThisObject
            val resID = param.args[0]

            val context = anim.getObjectFieldAs<Context>("mContext")
            if (normal == null) {
                normal = context.getDrawableIdByName("finger_circle_image_normal")
            }
            if (light == null) {
                light = context.getDrawableIdByName("finger_circle_image_light")
            }
            // aod = context.getDrawableIdByName("finger_circle_image_aod")

            if (resID == normal || resID == light) {
                param.args[0] = R.drawable.bg_transparent_1x1
            }
        }
    }
}
