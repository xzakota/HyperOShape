package com.xzakota.oshape.startup.rule.systemui.api

import com.xzakota.android.hook.startup.base.BaseReflectObject
import com.xzakota.code.extension.callMethodAs

class SelectedUserInteractor(instance: Any) : BaseReflectObject(instance) {
    fun getSelectedUserId(): Int = instance.callMethodAs("getSelectedUserId")
}
