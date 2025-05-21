package com.xzakota.oshape.startup.rule.systemui

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.doOnAttach
import androidx.core.view.isVisible
import com.xzakota.android.extension.view.findViewByIdName
import com.xzakota.android.extension.view.parentGroup
import com.xzakota.android.extension.view.updateLayoutParams
import com.xzakota.android.hook.extension.afterHookedMethodByName
import com.xzakota.android.util.DensityUtils.dp2px
import com.xzakota.code.extension.callMethodAs
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.isMiniWindowBarHeight
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.isMiniWindowBarHide
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.isMiniWindowBarWidth
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.miuiExpandableNotificationRow

object MiniWindowBarSize : BaseHook() {
    override fun onHookLoad() {
        if (isMiniWindowBarHide) {
            resHooker.hookLayout(nonNullBasePackageName, "notification_material_action_list") { param ->
                param.view.findViewByIdName("actions_container_layout")?.doOnAttach {
                    it.updateLayoutParams<FrameLayout.LayoutParams> {
                        bottomMargin = if (it.findViewByIdName("actions")?.isVisible == true) {
                            dp2px(15)
                        } else {
                            0
                        }
                    }
                }
            }

            miuiExpandableNotificationRow.afterHookedMethodByName("updateMiniWindowBar") { param ->
                val row = param.thisObjectAs<ViewGroup>()
                row.callMethodAs<View>("getMMiniBar").isVisible = false
            }
        } else {
            resHooker.hookLayout(nonNullBasePackageName, "heads_up_mini_window_bar") { param ->
                param.view.parentGroup?.findViewByIdName("mini_window_bar")?.doOnAttach {
                    it.updateLayoutParams<FrameLayout.LayoutParams> {
                        if (isMiniWindowBarWidth) {
                            width = dp2px(prefs.getInt("systemui_notification_center_mini_window_bar_width"))
                        }

                        if (isMiniWindowBarHeight) {
                            height = dp2px(prefs.getInt("systemui_notification_center_mini_window_bar_height"))
                        }
                    }
                }
            }

//            if (isMiniWindowBarWidth) {
//                resHooker.replaceDimen(
//                    nonNullBasePackageName,
//                    "mini_window_bar_width",
//                    prefs.getInt("systemui_notification_center_mini_window_bar_width")
//                )
//            }

//            if (isMiniWindowBarHeight) {
//                resHooker.replaceDimen(
//                    nonNullBasePackageName,
//                    "mini_window_bar_height",
//                    prefs.getInt("systemui_notification_center_mini_window_bar_height")
//                )
//            }
        }
    }
}
