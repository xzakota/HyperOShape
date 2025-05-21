package com.xzakota.oshape.startup.rule.home.api

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.xzakota.code.extension.callStaticMethodAs
import com.xzakota.code.extension.callStaticVoidMethod
import com.xzakota.oshape.startup.rule.home.shared.Blur.blurUtilities

object BlurUtilities {
    fun isBlurSupported(): Boolean = blurUtilities.callStaticMethodAs("isBlurSupported")

    fun isFolderBlurSupported(
        isFolderIcon1x1: Boolean
    ): Boolean = blurUtilities.callStaticMethodAs("isFolderBlurSupported", isFolderIcon1x1)

    fun isWidgetBlurSupported(): Boolean = blurUtilities.callStaticMethodAs("isWidgetBlurSupported")

    fun supportWidgetBackgroundBlur(
        view: View
    ): Boolean = blurUtilities.callStaticMethodAs("supportWidgetBackgroundBlur", view)

    fun getFolderIconBlurRoundRectRadius(
        context: Context,
        isFolderIcon1x1: Boolean,
        imageView: ImageView
    ): Int = blurUtilities.callStaticMethodAs("getFolderIconBlurRoundRectRadius", context, isFolderIcon1x1, imageView)

    fun setWidgetBackgroundBlendColors(
        view: View
    ) = blurUtilities.callStaticVoidMethod("setWidgetBackgroundBlendColors", view)
}
