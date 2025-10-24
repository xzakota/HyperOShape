package com.xzakota.oshape.startup.rule.systemui.statusbar

import android.content.Context
import android.widget.FrameLayout
import com.xzakota.android.extension.view.setPaddingRight
import com.xzakota.android.hook.extension.beforeHookedMethod
import com.xzakota.android.hook.startup.base.BaseRule
import com.xzakota.oshape.application.SystemShared.isMoreHyperOS3
import com.xzakota.oshape.startup.rule.systemui.api.MiuiConfigs
import com.xzakota.oshape.startup.rule.systemui.api.MiuiStub
import com.xzakota.reflect.extension.findField

class BluetoothBattery : PairTextView() {
    private var batteryLevel = 0

    override fun getSlot(): String = SLOT

    override fun bind(parent: FrameLayout, context: Context) {
        super.bind(parent, context)

        topView.run {
            if (isMoreHyperOS3) {
                setTextAppearance(getHostStyleIdByName("TextAppearance.StatusBar.Battery.Percent"))
                MiuiConfigs.applyStatusBarTypeface(MiuiConfigs.sMiproTypefaceWght500, this)
            } else {
                setTextAppearance(getHostStyleIdByName("TextAppearance.StatusBar.Clock"))
            }

            setPaddingRight(3)
        }
    }

    override fun isShowSingle(): Boolean = true

    override fun companionRule(): BaseRule? = BluetoothBatteryRule

    override fun updateView() {
        topView.text = "$batteryLevel"
    }

    override fun isBlockInRight(): Boolean = true
    override fun isBlockInMiniRight(): Boolean = true

    override fun insertIndexOf(array: List<String>): Int = array.indexOf("bluetooth_handsfree_battery") + 1

    private companion object BluetoothBatteryRule : BaseRule(false) {
        private const val SLOT = "oshape_bluetooth_battery"

        override fun onRuleLoad() {
            getClass("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarPolicy").beforeHookedMethod(
                "onBluetoothBatteryChange", Int::class.java
            ) { param ->
                val bluetoothBatteryLevel = param.nnThis.findField<Int>("mBluetoothBatteryLevel").get()
                val batteryLevel = param.args<Int>()

                MiuiStub.myProvider.statusBarIconController.findView<BluetoothBattery>(SLOT) {
                    it.batteryLevel = batteryLevel
                    if (batteryLevel == -1) {
                        it.setVisibleByController(false)
                        return@findView
                    }

                    if (bluetoothBatteryLevel != batteryLevel) {
                        it.setVisibleByController(true)
                    }
                }
            }
        }
    }
}
