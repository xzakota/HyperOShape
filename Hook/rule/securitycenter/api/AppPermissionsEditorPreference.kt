package com.xzakota.oshape.startup.rule.securitycenter.api

import android.content.Context
import com.xzakota.android.hook.startup.component.androidx.preference.RePreference
import com.xzakota.code.extension.callMethod
import com.xzakota.code.extension.findFirstFieldByType
import com.xzakota.code.extension.findFirstMethodOrNull
import com.xzakota.code.util.reflect.InvokeUtils.loadClass

class AppPermissionsEditorPreference : RePreference {
    constructor(instance: Any) : super(instance)
    constructor(context: Context) : super(context, CLASS)

    private var titleField = CLASS.findFirstFieldByType(String::class.java)
    private var isIconVisibleField = CLASS.findFirstFieldByType(java.lang.Boolean::class.java)
    private var getActionMethod = CLASS.superclass?.findFirstMethodOrNull(Int::class.java)
    private var setActionMethod = CLASS.superclass?.findFirstMethodOrNull(Void.TYPE, Int::class.java)

    override var title: String?
        get() = titleField.get(instance) as? String
        set(value) = titleField.set(instance, value)

    var isIconVisible: Boolean
        get() = isIconVisibleField.get(instance) as Boolean
        set(value) = isIconVisibleField.set(instance, value)

    var action: Int
        get() = getActionMethod?.invoke(instance) as? Int ?: ACTION_DEFAULT
        set(value) {
            setActionMethod?.invoke(instance, value)
        }

    fun setEnabled(isEnabled: Boolean) {
        instance.callMethod("setEnabled", isEnabled)
    }

    companion object {
        private var CLASS = loadClass("com.miui.permcenter.permissions.AppPermissionsEditorPreference")

        const val ACTION_DEFAULT = 0
        const val ACTION_REJECT = 1
        const val ACTION_PROMPT = 2
        const val ACTION_ACCEPT = 3
        const val ACTION_BLOCK = 4
        const val ACTION_NON_BLOCK = 5
        const val ACTION_FOREGROUND = 6
        const val ACTION_VIRTUAL = 7
    }
}
