package com.xzakota.oshape.startup.rule.securitycenter.api

import android.content.Context
import com.xzakota.android.hook.startup.component.androidx.preference.RePreference
import com.xzakota.code.extension.callMethod
import com.xzakota.code.extension.callMethodAs
import com.xzakota.code.util.reflect.InvokeUtils.loadClass

class ReTextPreference : RePreference {
    constructor(instance: Any) : super(instance)
    constructor(context: Context) : super(context, TEXT_PREFERENCE)

    var text: CharSequence?
        get() = instance.callMethodAs("getText")
        set(value) {
            instance.callMethod("setText", value)
        }

    private companion object {
        var TEXT_PREFERENCE = loadClass("miuix.preference.TextPreference")
    }
}
