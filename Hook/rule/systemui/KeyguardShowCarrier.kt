package com.xzakota.oshape.startup.rule.systemui

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.xzakota.android.extension.view.findViewByIdNameAs
import com.xzakota.android.extension.view.parentGroup
import com.xzakota.android.hook.extension.afterHookedMethod
import com.xzakota.android.hook.extension.beforeHookedMethod
import com.xzakota.android.hook.extension.reflect.createAfterHook
import com.xzakota.android.util.DensityUtils.dp2px
import com.xzakota.code.extension.createInstanceAs
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.code.util.reflect.InvokeUtils
import com.xzakota.oshape.R
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.SystemShared.isMoreHyperOS200
import com.xzakota.oshape.startup.rule.systemui.shared.Keyguard.miuiKeyguardStatusBarView

object KeyguardShowCarrier : BaseHook() {
    override fun onHookLoad() {
        miuiKeyguardStatusBarView.afterHookedMethod("onFinishInflate") { param ->
            val carrierText = param.thisObjectAs<View>().findViewByIdNameAs<TextView>("keyguard_carrier_text")
            if (isMoreHyperOS200) {
                val miuiCarrierTextLayout = loadClass("com.android.systemui.controlcenter.shade.MiuiCarrierTextLayout")
                miuiCarrierTextLayout.beforeHookedMethod("onMeasure", Int::class.java, Int::class.java) { param ->
                    val layout = param.thisObjectAs<View>()
                    if (!layout.isVisible) {
                        param.callSuperMethod(*param.args)
                        param.result = null
                    }
                }
                carrierText.parentGroup?.addView(
                    miuiCarrierTextLayout.createInstanceAs<View>(carrierText.context).apply {
                        id = R.id.carrier_layout
                    }
                )
            } else {
                carrierText.visibility = View.VISIBLE
                carrierText.maxWidth = carrierTextViewMaxWidth
            }
        }

        runCatching {
            InvokeUtils.findMethodBestMatch(miuiKeyguardStatusBarView, "showNextAlarm", Long::class.java)
        }.getOrNull()?.createAfterHook { param ->
            val statusBarView = param.thisObjectAs<View>()
            val alarmLayout = statusBarView.getObjectFieldAs<View?>("mAlarmLayout") ?: return@createAfterHook
            val carrierView = if (isMoreHyperOS200) {
                statusBarView.findViewById(R.id.carrier_layout)
            } else {
                statusBarView.findViewByIdNameAs<View>("keyguard_carrier_text")
            }

            carrierView.isVisible = !alarmLayout.isVisible
        }
    }

    // 运营商文本组件宽度
    private val carrierTextViewMaxWidth by lazy {
        dp2px(prefs.getInt("systemui_lockscreen_carrier_text_view_max_width", 100))
    }
}
