package com.xzakota.oshape.startup.rule.home.shared

import android.graphics.Point
import com.xzakota.android.util.ThemeUtils
import com.xzakota.oshape.model.entity.MiBlendBlur
import com.xzakota.oshape.startup.base.BaseShare
import com.xzakota.oshape.startup.base.getDefaultThemeNightColor
import com.xzakota.oshape.startup.base.getDefaultThemeNormalColor
import com.xzakota.oshape.startup.base.getThemeNightColor
import com.xzakota.oshape.startup.base.getThemeNormalColor

object Blur : BaseShare() {
    private const val WIDGET_MI_BLUR_BLEND_BLUR = "home_widget_mi_bg_blend_blur"

    val isWidgetBlurFollowHookTheme by lazy {
        prefs.getBoolean("home_advanced_widget_mi_blur_follow_hook_theme_enabled")
    }

    val blurUtilities by lazy {
        loadClass("com.miui.home.launcher.common.BlurUtilities")
    }

    val blurPoints
        get() = if (ThemeUtils.isDarkTheme()) {
            nightBlurPoints
        } else {
            normalBlurPoints
        }

    private val normalBlurPoints by lazy {
        getWidgetBlurPoints(false)
    }

    private val nightBlurPoints by lazy {
        getWidgetBlurPoints(true)
    }

    private fun getWidgetBlurPoints(isDarkTheme: Boolean): ArrayList<Point>? {
        val list = arrayListOf<Point>()
        val blurs = getWidgetBlurs(isDarkTheme) ?: return null
        val colors = blurs.colorIntArray
        val modes = blurs.mode
        if (colors.size != modes.size) {
            return null
        }

        colors.forEachIndexed { index, c ->
            list.add(Point(c, modes[index]))
        }

        return list
    }

    private fun getWidgetBlurs(isDarkTheme: Boolean): MiBlendBlur? = if (isWidgetBlurFollowHookTheme) {
        if (isDarkTheme) {
            getThemeNightColor(WIDGET_MI_BLUR_BLEND_BLUR)
        } else {
            getThemeNormalColor(WIDGET_MI_BLUR_BLEND_BLUR)
        }
    } else {
        if (isDarkTheme) {
            getDefaultThemeNightColor(WIDGET_MI_BLUR_BLEND_BLUR)
        } else {
            getDefaultThemeNormalColor(WIDGET_MI_BLUR_BLEND_BLUR)
        }
    }
}
