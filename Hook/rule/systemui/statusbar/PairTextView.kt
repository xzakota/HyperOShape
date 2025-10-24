package com.xzakota.oshape.startup.rule.systemui.statusbar

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import com.xzakota.oshape.R
import com.xzakota.oshape.databinding.LayoutViewPairTextBinding
import com.xzakota.oshape.startup.rule.systemui.api.DarkIconDispatcher

abstract class PairTextView : IDisplayableStatusBarView() {
    protected lateinit var topView: TextView
    protected lateinit var bottomView: TextView

    override fun bind(parent: FrameLayout, context: Context) {
        val inflate = LayoutViewPairTextBinding.inflate(LayoutInflater.from(context), parent, true).root
        val parentLayout = inflate.findViewById<FrameLayout>(R.id.pair_root)

        topView = parentLayout.findViewById(R.id.pair_first_view)
        if (!isShowSingle()) {
            bottomView = parentLayout.findViewById(R.id.pair_second_view)
        } else {
            topView.translationY = 0F
            topView.updateLayoutParams {
                height = LinearLayout.LayoutParams.MATCH_PARENT
            }
        }
    }

    override fun updateLightDarkTint() {
        val color = if (isUseTint) {
            DarkIconDispatcher.getTint(tintAreas, topView, viewTint)
        } else {
            if (DarkIconDispatcher.getDarkIntensity(tintAreas, topView, darkIntensity) > 0F) {
                lightColor
            } else {
                darkColor
            }
        }

        topView.setTextColor(color)
        if (!isShowSingle()) {
            bottomView.setTextColor(color)
        }
    }

    protected open fun isShowSingle(): Boolean = false
}
