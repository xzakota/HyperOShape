package com.xzakota.oshape.startup.rule.systemui

import com.xzakota.android.hook.extension.afterHookedFirstConstructor
import com.xzakota.android.hook.extension.chainSetObjectFieldWith
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.util.StandardFolder

object ManageGesturesDat : BaseHook() {
    override fun onHookLoad() {
        loadClass("com.android.systemui.statusbar.phone.CentralSurfacesImpl").afterHookedFirstConstructor { param ->
            param.nonNullThisObject.chainSetObjectFieldWith("mGestureRec.mLogfile") { file ->
                StandardFolder.replacePathUnderMIUI(file as String, "statusbar_gestures.dat")
            }
        }
    }
}
