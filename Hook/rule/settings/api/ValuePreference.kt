package com.xzakota.oshape.startup.rule.settings.api

import android.content.Context
import com.xzakota.android.hook.startup.component.androidx.preference.RePreference
import com.xzakota.code.extension.callMethodAs
import com.xzakota.code.extension.callVoidMethod
import com.xzakota.code.util.reflect.InvokeUtils.loadClass

class ValuePreference : RePreference {
    constructor(instance: Any) : super(instance)
    constructor(context: Context) : super(context, CLASS)

    var value: String?
        get() = instance.callMethodAs("getValue")
        set(value) = instance.callVoidMethod("setValue", value)

    fun setValue(redID: Int) = instance.callVoidMethod("setValue", redID)

    private companion object {
        val CLASS = loadClass("com.android.settingslib.miuisettings.preference.ValuePreference")
    }
}
