package com.xzakota.oshape.startup.rule.systemui

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.forEach
import com.xzakota.android.extension.view.findViewById
import com.xzakota.android.extension.view.findViewByIdNameAs
import com.xzakota.android.hook.extension.hookMethod
import com.xzakota.code.extension.getBooleanField
import com.xzakota.code.extension.setBooleanField
import com.xzakota.oshape.R
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.SystemShared.isMoreHyperOS200
import com.xzakota.oshape.startup.rule.systemui.api.Dependency
import com.xzakota.oshape.startup.rule.systemui.shared.Keyguard.miuiKeyguardStatusBarView
import com.xzakota.oshape.startup.rule.systemui.shared.Keyguard.statusBarMode

object KeyguardStatusBarMode : BaseHook() {
    override fun onHookLoad() {
        miuiKeyguardStatusBarView.hookMethod("updateIconsAndTextColors") {
            var lightLockScreenWallpaper = false

            before { param ->
                val view = param.thisObjectAs<View>()
                val isLight = statusBarMode == 1

                lightLockScreenWallpaper = view.getBooleanField("mLightLockScreenWallpaper")
                view.setBooleanField("mForceBlack", false)
                view.setBooleanField("mLightLockScreenWallpaper", isLight)

                val darkIconDispatcher = Dependency.darkIconDispatcher ?: return@before
                val color = darkIconDispatcher.getIconColorSingleTone(isLight)
                if (isMoreHyperOS200) {
                    view.findViewById(R.id.carrier_layout) {
                        it as ViewGroup
                        it.forEach { child ->
                            if (child is TextView) {
                                child.setTextColor(color)
                            } else if (child is ImageView) {
                                child.imageTintList = ColorStateList.valueOf(color)
                            }
                        }
                    }
                } else {
                    view.findViewByIdNameAs<TextView>("keyguard_carrier_text").setTextColor(color)
                }
            }

            after { param ->
                param.nonNullThisObject.setBooleanField("mLightLockScreenWallpaper", lightLockScreenWallpaper)
            }
        }
    }
}
