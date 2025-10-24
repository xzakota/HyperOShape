package com.xzakota.oshape.startup.rule.systemui.statusbar

import android.content.Context
import android.widget.FrameLayout
import com.xzakota.oshape.model.entity.BatteryBean
import com.xzakota.oshape.startup.rule.systemui.helper.BatteryHelper
import com.xzakota.oshape.startup.rule.systemui.shared.StatusBar.batteryStateMethod

class PhoneBatteryState : PairTextView(), BatteryHelper.OnBatteryChangedListener {
    override fun getSlot(): String = "oshape_phone_battery_state"

    init {
        BatteryHelper.start()
    }

    override fun bind(parent: FrameLayout, context: Context) {
        super.bind(parent, context)

        BatteryHelper.addListener(this)
        setVisibleByController(true)
    }

    override fun updateView() {
        updateView(BatteryHelper.GLOBAL_BATTERY)
    }

    override fun onBatteryChanged(bean: BatteryBean) {
        updateView(bean)
    }

    override fun isBlockInRight(): Boolean = batteryStateMethod == 3
    override fun isBlockInMiniRight(): Boolean = batteryStateMethod == 3

    override fun insertIndexOf(array: List<String>): Int {
        val positionOf = prefsMap.getString("systemui_status_bar_battery_state_position_of")
        return array.indexOf(positionOf) + 1
    }

    private fun updateView(bean: BatteryBean) {
        val pair = when (content) {
            0 -> bean.getTemperatureText() to bean.getCurrentText(isTakeAbsValue)
            1 -> bean.getTemperatureText() to bean.getPowerText(isTakeAbsValue)
            else -> bean.getCurrentText(isTakeAbsValue) to bean.getPowerText(isTakeAbsValue)
        }

        topView.text = pair.first
        bottomView.text = pair.second
    }

    private val content by lazy {
        prefsMap.getStringAsInt("systemui_status_bar_battery_state_content", 0)
    }

    private val isTakeAbsValue by lazy {
        prefsMap.getBoolean("systemui_status_bar_battery_state_always_take_abs_value_enabled")
    }
}
