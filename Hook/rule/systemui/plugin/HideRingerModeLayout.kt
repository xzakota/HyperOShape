package com.xzakota.oshape.startup.rule.systemui.plugin

import android.content.Context
import android.view.View
import androidx.core.view.isVisible
import com.xzakota.android.hook.extension.afterHookedMethod
import com.xzakota.android.hook.extension.beforeHookedMethod
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.systemui.api.Plugin
import com.xzakota.oshape.startup.rule.systemui.api.PluginInstance
import com.xzakota.oshape.startup.rule.systemui.api.PluginInstance.Companion.PLUGIN_VOLUME_DIALOG
import com.xzakota.oshape.startup.rule.systemui.api.PluginListener

object HideRingerModeLayout : BaseHook() {
    override fun onHookLoad() {
        PluginInstance.addListener(object : PluginListener(PluginInstance.miuiSystemUI(PLUGIN_VOLUME_DIALOG)) {
            override fun onPluginBeforeLoad(plugin: Plugin, pluginContext: Context?, pluginInstance: PluginInstance) {
                val ringerModeLayout = pluginInstance.loadClass("com.android.systemui.miui.volume.MiuiRingerModeLayout")

                addHook(ringerModeLayout.afterHookedMethod("onFinishInflate") {
                    it.thisObjectAs<View>().isVisible = false
                })

                addHook(ringerModeLayout.beforeHookedMethod("setTimerShowingState", Boolean::class.java) {
                    if (it.argAs()) {
                        it.thisObjectAs<View>().isVisible = false
                        it.result = null
                    }
                })
            }
        })
    }
}
