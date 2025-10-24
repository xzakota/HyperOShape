package com.xzakota.oshape.startup.rule.systemui.statusbar

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.xzakota.android.hook.extension.afterHookedMethod
import com.xzakota.android.hook.extension.afterHookedMethodByName
import com.xzakota.android.hook.extension.beforeHookedMethod
import com.xzakota.android.hook.extension.replaceMethod
import com.xzakota.oshape.startup.base.BaseRule
import com.xzakota.oshape.startup.rule.systemui.shared.StatusBar.isHideBatteryIcon
import com.xzakota.oshape.startup.rule.systemui.shared.StatusBar.isHideBatteryPercent
import com.xzakota.reflect.extension.callMethod
import com.xzakota.reflect.extension.callMethodAs
import com.xzakota.reflect.extension.findField
import kotlin.math.max

object HideBatteryView : BaseRule() {
    override fun onRuleLoad() {
        val batteryMeterView = getClass("com.android.systemui.statusbar.views.MiuiBatteryMeterView")
        if (isHideBatteryIcon) {
            batteryMeterView.beforeHookedMethod("onBatteryStyleChanged", Int::class.java) { param ->
                val viewGroup = param.nnThis

                viewGroup.findField<View>("mBatteryIconView").get().isVisible = false
                viewGroup.findField<View>("mHollowBatteryIconView").get().isVisible = false
                viewGroup.findField<View>("mBatteryDigitalView").get().isVisible = true
                viewGroup.findField<View>("mBatteryPercentMarkView").get().isVisible = !isHideBatteryPercent

                viewGroup.findField<Int>("mBatteryStyle").set(3)
                viewGroup.callMethod("updateChargeAndText")

                param.result = null
            }

            forHideIcon(batteryMeterView)
        }

        if (isHideBatteryPercent && !isHideBatteryIcon) {
            batteryMeterView.afterHookedMethod("updateChargeAndText") { param ->
                param.nnThis.findField<View>("mBatteryPercentMarkView").get().isVisible = false
            }
        }
    }

    private fun forHideIcon(batteryMeterView: Class<*>) {
        batteryMeterView.beforeHookedMethod("onDarkChangedInternal") { param ->
            val viewGroup = param.thisObject<ViewGroup>()
            if (viewGroup.findField<Boolean>("mCharging").get()) {
                val chargingView = viewGroup.findField<View>("mBatteryChargingView").get()

                val isUseAnimate = viewGroup.callMethodAs<Boolean>("getUseAnimate")
                val isFolmeNotNull = viewGroup.callMethodAs<Boolean>("folmeNotNull")
                if (isUseAnimate && isFolmeNotNull) {
                    viewGroup.callMethod(
                        "showView",
                        viewGroup.findField<Any>("mBatteryChargeingViewFolme").get(),
                        chargingView,
                        "battery_charging_view"
                    )
                } else {
                    chargingView.isVisible = true
                }
            }

            viewGroup.requestLayout()
        }

        batteryMeterView.afterHookedMethodByName("onLayout") { param ->
            val viewGroup = param.thisObject<ViewGroup>()
            // val changed = param.args<Boolean>()
            // val l = param.args<Int>(1)
            // val t = param.args<Int>(2)
            // val r = param.args<Int>(3)
            // val b = param.args<Int>(4)

            val parentWidth = viewGroup.measuredWidth
            val parentHeight = viewGroup.measuredHeight

            // val isLayoutRtl = viewGroup.callMethodAs<Boolean>("isLayoutRtl")
            val batteryChargingView = viewGroup.findField<View?>("mBatteryChargingView").get()
            if (batteryChargingView != null && batteryChargingView.isVisible) {
                val childWidth = batteryChargingView.measuredWidth
                val childHeight = batteryChargingView.measuredHeight

                val childLeft = parentWidth - childWidth
                val childRight = parentWidth
                val childTop = (parentHeight - childHeight) / 2
                val childBottom = childTop + childHeight

                batteryChargingView.layout(childLeft, childTop, childRight, childBottom)
            }
        }

        batteryMeterView.afterHookedMethodByName("onLayout") { param ->
            val viewGroup = param.thisObject<ViewGroup>()
            // val changed = param.args<Boolean>()
            // val l = param.args<Int>(1)
            // val t = param.args<Int>(2)
            // val r = param.args<Int>(3)
            // val b = param.args<Int>(4)

            val parentWidth = viewGroup.measuredWidth
            val parentHeight = viewGroup.measuredHeight

            // val isLayoutRtl = viewGroup.callMethodAs<Boolean>("isLayoutRtl")
            val batteryChargingView = viewGroup.findField<View?>("mBatteryChargingView").get()
            if (batteryChargingView != null && batteryChargingView.isVisible) {
                val childWidth = batteryChargingView.measuredWidth
                val childHeight = batteryChargingView.measuredHeight

                val childLeft = parentWidth - childWidth
                val childRight = parentWidth
                val childTop = (parentHeight - childHeight) / 2
                val childBottom = childTop + childHeight

                batteryChargingView.layout(childLeft, childTop, childRight, childBottom)
            }
        }

        batteryMeterView.beforeHookedMethod("onMeasure", Int::class.java, Int::class.java) { param ->
            val viewGroup = param.thisObject<ViewGroup>()
            val widthMeasureSpec = param.args<Int>()
            val heightMeasureSpec = param.args<Int>(1)

            var finalWidth = 0
            var maxHeight = 0

            val digitalView = viewGroup.findField<View?>("mBatteryDigitalView").get()
            if (digitalView != null && digitalView.isVisible) {
                viewGroup.callMethod(
                    "measureChildWithMargins",
                    digitalView, widthMeasureSpec, 0, heightMeasureSpec, 0
                )

                finalWidth = digitalView.measuredWidth
                maxHeight = max(0, digitalView.measuredHeight)
            }

            val batteryPercentContainer = viewGroup.findField<View?>("mBatteryPercentContainer").get()
            if (batteryPercentContainer != null && batteryPercentContainer.isVisible) {
                viewGroup.callMethod(
                    "measureChildWithMargins",
                    batteryPercentContainer, widthMeasureSpec, finalWidth, heightMeasureSpec, 0
                )

                finalWidth += batteryPercentContainer.measuredWidth
                maxHeight = max(maxHeight, batteryPercentContainer.measuredHeight)
            }

            val isCharging = viewGroup.findField<Boolean>("mCharging").get()
            val batteryChargingView = viewGroup.findField<View?>("mBatteryChargingView").get()
            if (batteryChargingView != null && isCharging && batteryChargingView.isVisible) {
                viewGroup.callMethod(
                    "measureChildWithMargins",
                    batteryChargingView, widthMeasureSpec, finalWidth + 5, heightMeasureSpec, 0
                )

                finalWidth += batteryChargingView.measuredWidth + 5
                maxHeight = max(maxHeight, batteryChargingView.measuredHeight)
            }

            viewGroup.callMethod(
                "setMeasuredDimension",
                viewGroup.paddingEnd + viewGroup.paddingStart + finalWidth,
                viewGroup.paddingBottom + viewGroup.paddingTop + maxHeight
            )
            viewGroup.pivotY = viewGroup.height / 2F

            param.result = null
        }
    }
}
