package com.xzakota.oshape.startup.rule.home.api

import android.content.Context
import android.util.AttributeSet
import com.xzakota.android.hook.startup.component.android.view.ReView
import com.xzakota.oshape.startup.rule.home.shared.HomeShared.fixedAspectRatioLottieAnimView

class FixedAspectRatioLottieAnimView(
    context: Context,
    attrs: AttributeSet?,
    clazz: Class<*> = fixedAspectRatioLottieAnimView
) : ReView(context, attrs, clazz)
