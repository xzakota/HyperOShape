package com.xzakota.oshape.startup.rule.systemui.plugin

import android.graphics.drawable.LayerDrawable
import android.widget.ImageView
import com.xzakota.android.extension.graphics.drawable.lastDrawable
import com.xzakota.android.hook.extension.afterHookedMethod
import com.xzakota.android.hook.extension.afterHookedMethodByName
import com.xzakota.android.hook.extension.getAdditionalInstanceFieldAs
import com.xzakota.android.hook.extension.setAdditionalInstanceField
import com.xzakota.code.extension.getFloatField
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.oshape.startup.rule.systemui.api.PluginInstance
import com.xzakota.oshape.startup.rule.systemui.api.PluginListener
import com.xzakota.oshape.widget.drawable.SVGDrawable

class AdaptiveTileIconSize(
    pluginInstance: PluginInstance,
    listener: PluginListener
) : BasePluginHook(pluginInstance, listener) {
    override fun onHookLoad() {
        val qsTileItem = loadClass("miui.systemui.controlcenter.qs.tileview.QSTileItemIconView")
        addHook(qsTileItem.afterHookedMethodByName("updateIcon") { param ->
            val iconView = param.nonNullThisObject
            val icon = iconView.getObjectFieldAs<ImageView>("icon")
            val isUpdateIconSize = iconView.getAdditionalInstanceFieldAs<Boolean?>("updateIconSize")

            val drawable = icon.drawable as? LayerDrawable
            if (drawable != null && isUpdateIconSize == false) {
                val last = drawable.lastDrawable
                if (last is SVGDrawable) {
                    val tileSize = iconView.getFloatField("customTileSize")
                    last.setSize(tileSize, tileSize)
                }

                iconView.setAdditionalInstanceField("updateIconSize", true)
                icon.invalidateOutline()
            }
        })

        addHook(qsTileItem.afterHookedMethod("updateIconSize") { param ->
            param.nonNullThisObject.setAdditionalInstanceField("updateIconSize", false)
        })
    }
}
