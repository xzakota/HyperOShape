package com.xzakota.oshape.startup.rule.systemui

import android.view.View
import com.xzakota.android.hook.extension.afterHookedMethodByName
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.SystemShared.isMoreHyperOS200
import com.xzakota.oshape.startup.rule.systemui.shared.ControlCenter.controlCenterHeaderController

object HideCCHeaderCarrier : BaseHook() {
    override fun onHookLoad() {
        controlCenterHeaderController.afterHookedMethodByName("updateCarrierAndPrivacyVisible") { param ->
            param.nonNullThisObject.getObjectFieldAs<View>(
                if (isMoreHyperOS200) {
                    "carrierLayout"
                } else {
                    "carrierView"
                }
            ).visibility = View.INVISIBLE
        }
    }
}
