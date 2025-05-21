package com.xzakota.oshape.startup.rule.systemui.api

import com.xzakota.android.hook.startup.base.BaseReflectObject
import com.xzakota.code.extension.getIntField

class DarkIconDispatcher(instance: Any) : BaseReflectObject(instance) {
    val lightModeIconColorSingleTone get() = instance.getIntField("mLightModeIconColorSingleTone")
    val darkModeIconColorSingleTone get() = instance.getIntField("mDarkModeIconColorSingleTone")

    fun getIconColorSingleTone(isLight: Boolean): Int = if (isLight) {
        darkModeIconColorSingleTone
    } else {
        lightModeIconColorSingleTone
    }
}
