package com.xzakota.oshape.startup.rule.systemui

import com.xzakota.android.hook.extension.replaceMethodByName
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.systemui.shared.Keyguard.keyguardMoveLeftController

object RemoveMagazineGesture : BaseHook() {
    override fun onHookLoad() {
        keyguardMoveLeftController.replaceMethodByName("isLeftViewLaunchActivity", false)
    }
}
