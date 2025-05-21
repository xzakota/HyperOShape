package com.xzakota.oshape.startup.rule.systemui

import com.xzakota.android.hook.extension.beforeHookedMethod
import com.xzakota.android.hook.extension.replaceMethodByName
import com.xzakota.code.extension.setObjectField
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.SystemShared.isMoreHyperOS200
import com.xzakota.oshape.startup.rule.systemui.shared.Keyguard.keyguardBottomAreaInjector

object RemoveKeyguardShortcutTips : BaseHook() {
    override fun onHookLoad() {
        if (isMoreHyperOS200) {
            keyguardBottomAreaInjector.replaceMethodByName("handleBottomButtonClickedAnimation")
            keyguardBottomAreaInjector.replaceMethodByName("updateAffordanceViewTipsLayoutParams")
        } else {
            keyguardBottomAreaInjector.replaceMethodByName("startButtonLayoutAnimate")
            keyguardBottomAreaInjector.replaceMethodByName("initTipsView")

            keyguardBottomAreaInjector.beforeHookedMethod("updateIcons") { param ->
                val injector = param.nonNullThisObject

                injector.setObjectField("mLeftAffordanceViewTips", null)
                injector.setObjectField("mRightAffordanceViewTips", null)
            }
        }
    }
}
