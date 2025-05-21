package com.xzakota.oshape.startup.rule.systemui.api

import android.content.Context
import android.view.View
import android.view.Window
import com.xzakota.android.hook.startup.base.BaseReflectObject
import com.xzakota.code.extension.callStaticMethodAs
import com.xzakota.code.extension.callVoidMethod
import com.xzakota.code.extension.createInstance
import com.xzakota.code.util.reflect.InvokeUtils.loadClass

object PluginBlurUtils {
    private var CLASS_LOADER: ClassLoader? = null

    private val BLUR_UTILS by lazy {
        loadClass("miui.systemui.util.BlurUtils", CLASS_LOADER)
    }

    fun init(classLoader: ClassLoader) {
        if (CLASS_LOADER == null || CLASS_LOADER != classLoader) {
            CLASS_LOADER = classLoader
        }
    }

    fun isFlipDevice(context: Context): Boolean = BLUR_UTILS.callStaticMethodAs("isFlipDevice", context)

    fun isFlipTinyScreen(context: Context): Boolean = BLUR_UTILS.callStaticMethodAs("isFlipTinyScreen", context)

    fun isTinyScreen(): Boolean = BLUR_UTILS.callStaticMethodAs("isTinyScreen")

    fun isLowEndDevice(): Boolean = BLUR_UTILS.callStaticMethodAs("isLowEndDevice")

    class Blur(context: Context) : BaseReflectObject(BLUR_UTILS.createInstance(context)) {
        fun destroy() = instance.callVoidMethod("destroy")

        fun updateBlurDisabled() = instance.callVoidMethod("updateBlurDisabled")

        fun setBackgroundBlur(
            view: View,
            radius: Float,
            window: Window
        ) = instance.callVoidMethod("setBackgroundBlur", view, radius, window)

        fun setBackgroundBlur(
            view: View,
            radius: Float,
            window: Window,
            windowBgColorResID: Int,
            viewBgColorResID: Int
        ) = instance.callVoidMethod("setBackgroundBlur", view, radius, window, windowBgColorResID, viewBgColorResID)
    }
}
