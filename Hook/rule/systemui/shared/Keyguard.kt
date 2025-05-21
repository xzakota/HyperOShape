package com.xzakota.oshape.startup.rule.systemui.shared

import com.xzakota.android.hook.startup.base.BaseShare

object Keyguard : BaseShare() {
    // 固定时钟组件
    val fixClock by lazy {
        prefs.getStringAsInt("systemui_lockscreen_fix_clock_widget", 0)
    }

    // 状态栏模式
    val statusBarMode by lazy {
        prefs.getStringAsInt("systemui_lockscreen_status_bar_mode", 0)
    }

    // 移除按钮提示
    val isRemoveButtonTips by lazy {
        prefs.getBoolean("systemui_lockscreen_remove_bottom_button_tips_enabled")
    }

    // 自定义底部按钮
    val isCustomizeBottomButton by lazy {
        prefs.getBoolean("systemui_lockscreen_customize_bottom_button_enabled")
    }

    // 底部按钮-左按钮
    val leftButtonType by lazy {
        prefs.getStringAsInt("systemui_lockscreen_bottom_left_button")
    }

    // 底部按钮-右按钮
    val rightButtonType by lazy {
        prefs.getStringAsInt("systemui_lockscreen_bottom_right_button")
    }

    // 底部按钮-按钮背景边距
    val buttonBgMargins by lazy {
        prefs.getInt("systemui_lockscreen_bottom_button_bg_margins", 80)
    }

    // 底部按钮-按钮正常颜色
    val buttonNormalBgColor by lazy {
        prefs.getInt("systemui_lockscreen_bottom_button_bg_normal")
    }

    // 底部按钮-按钮激活颜色
    val buttonActiveBgColor by lazy {
        prefs.getInt("systemui_lockscreen_bottom_button_bg_active")
    }

    // 底部按钮-按钮激活灵敏度(I)
    val activeSensitivityLevel1 by lazy {
        prefs.getInt("systemui_lockscreen_bottom_button_active_sensitivity_level1", 300).toLong()
    }

    // 底部按钮-按钮激活灵敏度(II)
    val activeSensitivityLevel2 by lazy {
        prefs.getInt("systemui_lockscreen_bottom_button_active_sensitivity_level2", 750).toLong()
    }

    val miuiKeyguardStatusBarView by lazy {
        loadClass("com.android.systemui.statusbar.phone.MiuiKeyguardStatusBarView")
    }

    val keyguardBottomAreaView by lazy {
        loadClass("com.android.systemui.statusbar.phone.KeyguardBottomAreaView")
    }

    val keyguardMoveLeftController by lazy {
        loadClass("com.android.keyguard.negative.KeyguardMoveLeftController")
    }

    val keyguardMoveRightController by lazy {
        loadClass("com.android.keyguard.KeyguardMoveRightController")
    }

    val keyguardBottomAreaInjector by lazy {
        loadClass("com.android.keyguard.injector.KeyguardBottomAreaInjector")
    }
}
