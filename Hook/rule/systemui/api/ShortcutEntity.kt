package com.xzakota.oshape.startup.rule.systemui.api

import android.graphics.drawable.Drawable
import com.xzakota.android.hook.startup.base.BaseReflectObject
import com.xzakota.code.extension.getBooleanField
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.code.extension.getStringField
import com.xzakota.code.extension.setBooleanField
import com.xzakota.code.extension.setObjectField
import com.xzakota.code.extension.setStringField

class ShortcutEntity(instance : Any) : BaseReflectObject(instance) {
    val uniqueTag get() = instance.getStringField("uniqueTag")

    var tag: String?
        get() = instance.getStringField("tag")
        set(value) = instance.setStringField("tag", value)

    var packageName: String?
        get() = instance.getStringField("packageName")
        set(value) = instance.setStringField("packageName", value)

    var targetPackage: String?
        get() = instance.getStringField("targetPackage")
        set(value) = instance.setStringField("targetPackage", value)

    var targetClass: String?
        get() = instance.getStringField("targetClass")
        set(value) = instance.setStringField("targetClass", value)

    var shortcutName: String?
        get() = instance.getStringField("shortcutName")
        set(value) = instance.setStringField("shortcutName", value)

    var action: String?
        get() = instance.getStringField("action")
        set(value) = instance.setStringField("action", value)

    var data: String?
        get() = instance.getStringField("data")
        set(value) = instance.setStringField("data", value)

    var isAvailable: Boolean
        get() = instance.getBooleanField("isAvailable")
        set(value) = instance.setBooleanField("isAvailable", value)

    var isSystemDefault: Boolean
        get() = instance.getBooleanField("isSystemDefault")
        set(value) = instance.setBooleanField("isSystemDefault", value)

    var drawable: Drawable?
        get() = instance.getObjectFieldAs("drawable")
        set(value) = instance.setObjectField("drawable", value)
}
