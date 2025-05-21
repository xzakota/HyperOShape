package com.xzakota.oshape.startup.rule.systemui.api

import com.xzakota.android.hook.startup.base.BaseReflectObject
import com.xzakota.code.extension.callVoidMethod

class GlobalActions {
    class GlobalActionsManager(instance: Any) : BaseReflectObject(instance) {
        fun onGlobalActionsHidden() = instance.callVoidMethod("onGlobalActionsHidden")

        fun onGlobalActionsShown() = instance.callVoidMethod("onGlobalActionsShown")

        fun reboot(isSafeMode: Boolean) = instance.callVoidMethod("reboot", isSafeMode)

        fun shutdown() = instance.callVoidMethod("shutdown")
    }
}
