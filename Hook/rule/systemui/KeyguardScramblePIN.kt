package com.xzakota.oshape.startup.rule.systemui

import android.view.View
import android.view.ViewGroup
import com.xzakota.android.extension.view.findViewByIdNameAs
import com.xzakota.android.hook.extension.afterHookedMethod
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.code.extension.setObjectField
import com.xzakota.oshape.startup.base.BaseHook

/**
 * copy from [HyperCeiler](https://github.com/ReChronoRain/HyperCeiler)
 */
object KeyguardScramblePIN : BaseHook() {
    override fun onHookLoad() {
        loadClass("com.android.keyguard.KeyguardPINView").afterHookedMethod("onFinishInflate") { param ->
            val pinView = param.thisObjectAs<View>()
            val views = pinView.getObjectFieldAs<Array<Array<View?>>>("mViews")
            val random = mutableListOf<View?>()

            for (row in 1..3) {
                for (col in 0..2) {
                    views[row][col]?.let {
                        random.add(it)
                    }
                }
            }
            random.add(views[4][1])
            random.shuffle()

            val row1 = pinView.findViewByIdNameAs<ViewGroup>("row1")
            val row2 = pinView.findViewByIdNameAs<ViewGroup>("row2")
            val row3 = pinView.findViewByIdNameAs<ViewGroup>("row3")
            val row4 = pinView.findViewByIdNameAs<ViewGroup>("row4")

            row1.removeAllViews()
            row2.removeAllViews()
            row3.removeAllViews()
            row4.removeViewAt(1)

            views[1] = arrayOf(random[0], random[1], random[2])
            row1.addView(random[0])
            row1.addView(random[1])
            row1.addView(random[2])

            views[2] = arrayOf(random[3], random[4], random[5])
            row2.addView(random[3])
            row2.addView(random[4])
            row2.addView(random[5])

            views[3] = arrayOf(random[6], random[7], random[8])
            row3.addView(random[6])
            row3.addView(random[7])
            row3.addView(random[8])

            views[4] = arrayOf(null, random[9], views[4][2])
            row4.addView(random[9], 1)

            pinView.setObjectField("mViews", views)
        }
    }
}
