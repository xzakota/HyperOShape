package com.xzakota.oshape.startup.base

import android.graphics.drawable.Drawable
import com.xzakota.code.util.JSONUtils
import com.xzakota.oshape.util.HookThemeManager

interface IThemeResource {
    fun getThemeIcon(name: String): Drawable? = HookThemeManager.getIcon(name)
}

inline fun <reified T> IThemeResource.getThemeNormalColor(name: String): T? = JSONUtils.parseObjectOrNull(
    HookThemeManager.getColors(name).toString()
)

inline fun <reified T> IThemeResource.getThemeNightColor(name: String): T? = JSONUtils.parseObjectOrNull(
    HookThemeManager.getColors(name + "_night").toString()
)

inline fun <reified T> IThemeResource.getDefaultThemeNormalColor(name: String): T? = JSONUtils.parseObjectOrNull(
    HookThemeManager.getDefaultColors(name).toString()
)

inline fun <reified T> IThemeResource.getDefaultThemeNightColor(name: String): T? = JSONUtils.parseObjectOrNull(
    HookThemeManager.getDefaultColors(name + "_night").toString()
)
