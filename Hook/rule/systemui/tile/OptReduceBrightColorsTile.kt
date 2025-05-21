package com.xzakota.oshape.startup.rule.systemui.tile

import com.xzakota.android.hook.extension.afterHookedMethodByName
import com.xzakota.android.hook.extension.beforeHookedMethod
import com.xzakota.oshape.R
import com.xzakota.oshape.startup.base.BaseHook

object OptReduceBrightColorsTile : BaseHook() {
    override fun onHookLoad() {
        val reduceBrightColorsTile = loadClass("com.android.systemui.qs.tiles.ReduceBrightColorsTile")
        reduceBrightColorsTile.beforeHookedMethod("getTileLabel") { param ->
            param.result = getHostString(R.string.reduce_bright_colors)
        }

        reduceBrightColorsTile.afterHookedMethodByName("handleUpdateState") { param ->
            val state = TileManagement.TileBooleanState(param.argAs())

            state.label = getHostString(R.string.reduce_bright_colors)
            state.icon = TileManagement.TileDrawableIcon.get(getThemeIcon("extra_dim_mode"))
        }
    }
}
