package com.xzakota.oshape.startup.rule.home.layout

import android.view.View
import android.widget.FrameLayout
import com.xzakota.android.extension.view.updateLayoutParams
import com.xzakota.android.hook.extension.afterHookedMethodByName
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.oshape.startup.base.BaseHook

object HidePageIndicator : BaseHook() {
    override fun onHookLoad() {
        loadClass("com.miui.home.launcher.ScreenView").afterHookedMethodByName("setSeekBarPosition") { param ->
            param.nonNullThisObject.getObjectFieldAs<View?>("mScreenSeekBar")
                ?.updateLayoutParams<FrameLayout.LayoutParams> {
                    width = 0
                    height = 0
                }
        }
    }
}
