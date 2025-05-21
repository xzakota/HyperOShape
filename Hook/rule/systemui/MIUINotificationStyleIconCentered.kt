package com.xzakota.oshape.startup.rule.systemui

import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import com.xzakota.android.extension.view.updateLayoutParams
import com.xzakota.android.hook.extension.afterHookedMethodByName
import com.xzakota.code.extension.callMethodAs
import com.xzakota.code.extension.getIntField
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.oshape.startup.base.BaseHook

object MIUINotificationStyleIconCentered : BaseHook() {
    private val imIconTopMargin by lazy {
        getHostDimensionPixelSize("notification_app_im_icon_margin") -
            getHostDimensionPixelSize("notification_app_icon_margin")
    }

    override fun onHookLoad() {
        resHooker.hookLayout(nonNullBasePackageName, "notification_template_part_app_icon") { param ->
            param.view.updateLayoutParams<FrameLayout.LayoutParams> {
                gravity = Gravity.CENTER_VERTICAL
                topMargin = 0
            }
        }

        resHooker.hookLayout(nonNullBasePackageName, "notification_template_part_app_im_icon") { param ->
            param.view.updateLayoutParams<FrameLayout.LayoutParams> {
                gravity = Gravity.CENTER_VERTICAL
                topMargin = imIconTopMargin / 2
            }
        }

        loadClass("com.android.systemui.statusbar.notification.stack.MiuiNotificationChildrenContainer")
            .afterHookedMethodByName("onLayout") { param ->
                val container = param.nonNullThisObject
                val appIconSize = container.getIntField("mMiuiAppIconSize")
                val appIconMargin = container.getIntField("mMiuiAppIconMargin")
                val collapsedHeight = container.callMethodAs<Int>("getCollapsedHeight")
                val verticalMargin = (collapsedHeight - appIconSize) / 2

                container.getObjectFieldAs<View?>("mAppIcon")?.layout(
                    appIconMargin, verticalMargin, appIconMargin + appIconSize, verticalMargin + appIconSize
                )
            }
    }
}
