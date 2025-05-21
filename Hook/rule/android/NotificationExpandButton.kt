package com.xzakota.oshape.startup.rule.android

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.ImageView
import com.xzakota.android.hook.extension.afterHookedMethod
import com.xzakota.code.extension.getBooleanField
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.oshape.startup.base.BaseHook

object NotificationExpandButton : BaseHook() {
    override fun onHookLoad() {
        when (Build.VERSION.SDK_INT) {
            Build.VERSION_CODES.VANILLA_ICE_CREAM -> {
                val notificationExpandButton = loadClass("com.android.internal.widget.NotificationExpandButton")

                notificationExpandButton.afterHookedMethod("updateExpandedState") { param ->
                    val button = param.nonNullThisObject
                    val icon = button.getObjectFieldAs<ImageView>("mIconView")
                    val isExpanded = button.getBooleanField("mExpanded")

                    icon.setImageDrawable(
                        getThemeIcon(
                            if (isExpanded) {
                                "collapse_notification"
                            } else {
                                "expand_notification"
                            }
                        )
                    )
                }

                // 透明背景
                if (prefs.getBoolean("android_notification_expand_button_transparent_bg_enabled")) {
                    notificationExpandButton.afterHookedMethod("updateColors") { param ->
                        val button = param.nonNullThisObject
                        val pillDrawable = button.getObjectFieldAs<Drawable>("mPillDrawable")
                        pillDrawable.setTintList(ColorStateList.valueOf(Color.TRANSPARENT))
                    }
                }
            }
        }
    }
}
