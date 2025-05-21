package com.xzakota.oshape.startup.rule.systemui.tile

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import com.xzakota.android.extension.content.getString
import com.xzakota.android.hook.core.loader.base.IMemberHookParam
import com.xzakota.android.hook.extension.afterHookedFirstConstructor
import com.xzakota.android.hook.extension.afterHookedMethod
import com.xzakota.android.hook.extension.beforeHookedMethod
import com.xzakota.android.hook.extension.beforeHookedMethodByName
import com.xzakota.android.hook.extension.chainGetObject
import com.xzakota.android.hook.extension.createInstance
import com.xzakota.android.hook.extension.getAdditionalInstanceField
import com.xzakota.android.hook.extension.reflect.createBeforeHooks
import com.xzakota.android.hook.extension.reflect.methodFinder
import com.xzakota.android.hook.extension.removeAdditionalInstanceField
import com.xzakota.android.hook.extension.setAdditionalInstanceField
import com.xzakota.android.hook.startup.base.BaseReflectObject
import com.xzakota.code.extension.callMethod
import com.xzakota.code.extension.callMethodAs
import com.xzakota.code.extension.callStaticMethodAs
import com.xzakota.code.extension.createInstance
import com.xzakota.code.extension.getBooleanField
import com.xzakota.code.extension.getIntField
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.code.extension.getStringField
import com.xzakota.code.extension.reflect.isNotAbstract
import com.xzakota.code.extension.setBooleanField
import com.xzakota.code.extension.setIntField
import com.xzakota.code.extension.setObjectField
import com.xzakota.code.extension.setStringField
import com.xzakota.oshape.R
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.systemui.api.ActivityStarter
import com.xzakota.oshape.startup.rule.systemui.api.Plugin
import com.xzakota.oshape.startup.rule.systemui.api.PluginInstance
import com.xzakota.oshape.startup.rule.systemui.api.PluginInstance.Companion.PLUGIN_LOCAL_MIUI_TILE
import com.xzakota.oshape.startup.rule.systemui.api.PluginListener
import com.xzakota.oshape.startup.rule.systemui.plugin.AdaptiveTileIconSize
import com.xzakota.oshape.startup.rule.systemui.shared.ControlCenter.miuiQSFactory
import com.xzakota.oshape.startup.rule.systemui.shared.ControlCenter.tileAdapterState
import com.xzakota.oshape.startup.rule.systemui.shared.ControlCenter.tileBooleanState
import com.xzakota.oshape.startup.rule.systemui.shared.ControlCenter.tileDrawableIcon
import com.xzakota.oshape.startup.rule.systemui.shared.ControlCenter.tileResourceIcon
import com.xzakota.oshape.startup.rule.systemui.shared.ControlCenter.tileState
import kotlin.properties.Delegates

object TileManagement : BaseHook(false) {
    const val STATE_DISABLED = 0
    const val STATE_OFF = 1
    const val STATE_ON = 2

    private val tiles = mutableMapOf<String, BaseCustomTile>()
    var context by Delegates.notNull<Context>()
    var activityStarter: ActivityStarter? = null

    override fun onHookLoad() {
        val proxyTileClass = loadClass("com.android.systemui.qs.tiles.ScreenLockTile")
        val proxyTileProvider = "screenLockTileProvider"

        val rawTiles = requireHostContext().getString("miui_quick_settings_tiles_stock")

        addTiles()
        if (tiles.isEmpty()) {
            return
        }

        val newTiles = rawTiles + "," + tiles.keys.joinToString(",")
        resHooker.replaceString(nonNullBasePackageName, "miui_quick_settings_tiles_stock", newTiles)

        hookPluginTileItem()

        miuiQSFactory.beforeHookedMethod("createTile", String::class.java) { param ->
            val spec = param.argAs<String>()
            val tile = tiles[spec] ?: return@beforeHookedMethod

            val qsTile = param.nonNullThisObject.chainGetObject("$proxyTileProvider.get()")
            if (qsTile != null && qsTile.getBooleanField("mInControlCenter")) {
                qsTile.setAdditionalInstanceField("spec", spec)

                val state = tile.newTileState()
                state.copyTo(qsTile.getObjectFieldAs("mTmpState"))
                state.copyTo(qsTile.getObjectFieldAs("mState"))

                val handler = qsTile.getObjectFieldAs<Handler>("mHandler")
                handler.sendEmptyMessage(12)
                handler.sendEmptyMessage(11)

                qsTile.callMethod("setTileSpec", spec)
                param.result = qsTile
            } else {
                param.result = null
            }
        }

        proxyTileClass.afterHookedFirstConstructor { param ->
            if (activityStarter != null) {
                return@afterHookedFirstConstructor
            }

            val proxyTile = param.nonNullThisObject
            context = proxyTile.getObjectFieldAs("mContext")
            activityStarter = ActivityStarter(proxyTile.getObjectFieldAs("mActivityStarter"))
        }

        proxyTileClass.beforeHookedMethodByName("isAvailable", true) { param ->
            val tile = getTileFromParam(param) ?: return@beforeHookedMethodByName
            param.result = tile.isAvailable()
        }

        proxyTileClass.beforeHookedMethod("getTileLabel") { param ->
            val tile = getTileFromParam(param) ?: return@beforeHookedMethod
            param.result = tile.customTile.getLabel()
        }

        proxyTileClass.methodFinder().findSuper().filter {
            name in setOf("click", "longClick", "secondaryClick") && isNotAbstract
        }.toList().createBeforeHooks { param ->
            val tile = getTileFromParam(param) ?: return@createBeforeHooks

            val realTile = param.nonNullThisObject
            if (tile.isTileReady(realTile)) {
                tile.onRealTileCreated(realTile)
            }
        }

        proxyTileClass.beforeHookedMethod("getLongClickIntent", findSuper = true) { param ->
            val tile = getTileFromParam(param) ?: return@beforeHookedMethod
            param.result = tile.getLongClickIntent()
        }

        proxyTileClass.beforeHookedMethodByName("handleClick") { param ->
            val tile = getTileFromParam(param) ?: return@beforeHookedMethodByName
            tile.onClick()
            param.result = null
        }

        proxyTileClass.beforeHookedMethodByName("handleLongClick", true) { param ->
            val tile = getTileFromParam(param) ?: return@beforeHookedMethodByName
            if (tile.onLongClick()) {
                param.result = null
            }
        }

        proxyTileClass.beforeHookedMethodByName("handleSecondaryClick", true) { param ->
            val tile = getTileFromParam(param) ?: return@beforeHookedMethodByName
            if (tile.onSecondaryClick()) {
                param.result = null
            }
        }

        proxyTileClass.beforeHookedMethodByName("handleUpdateState") { param ->
            val tile = getTileFromParam(param) ?: return@beforeHookedMethodByName
            val state = param.argAs<Any>()
            tile.onUpdateState(
                if (tileBooleanState.isInstance(state)) {
                    TileBooleanState(state)
                } else {
                    TileState(state)
                },
                param.args[1]
            )

            if (tile.isTileReady()) {
                tile.state.copyFrom(param.nonNullThisObject.getObjectFieldAs("mTmpState"))
            }

            param.result = null
        }

        proxyTileClass.beforeHookedMethod("handleShowStateMessage", findSuper = true) { param ->
            val tile = getTileFromParam(param) ?: return@beforeHookedMethod
            if (tile.handleShowStateMessage()) {
                param.result = null
            }
        }

        proxyTileClass.beforeHookedMethodByName("handleUserSwitch", true) { param ->
            val tile = getTileFromParam(param) ?: return@beforeHookedMethodByName
            if (tile.handleUserSwitch()) {
                param.result = null
            }
        }

        proxyTileClass.beforeHookedMethodByName("getMetricsCategory") { param ->
            val tile = getTileFromParam(param) ?: return@beforeHookedMethodByName
            param.result = tile.getMetricsCategory()
        }

        proxyTileClass.afterHookedMethod("handleDestroy", findSuper = true) { param ->
            val tile = getTileFromParam(param) ?: return@afterHookedMethod
            val realTile = param.nonNullThisObject
            if (tile.isTileReady(realTile)) {
                tile.onDestroy()
            }
            realTile.removeAdditionalInstanceField("spec")
        }
    }

    private fun addTiles() {
        tiles.clear()

        mapOf(
            "systemui_control_center_lsposed_tile_enabled" to LSPosedTile::class.java,
            "systemui_control_center_sunlight_mode_tile_enabled" to SunlightModeTile::class.java,
            "systemui_control_center_invisible_mode_tile_enabled" to InvisibleModeTile::class.java
        ).forEach { (isEnabled, tileClass) ->
            if (prefs.getBoolean(isEnabled)) {
                val tile = tileClass.createInstance()
                tiles[tile.spec] = tile

                logI("Pre create tile ${tile.spec}")
            }
        }
    }

    private fun getTileFromParam(param: IMemberHookParam): BaseCustomTile? {
        val spec = param.nonNullThisObject.getAdditionalInstanceField("spec") ?: return null
        return tiles[spec]
    }

    private fun hookPluginTileItem() {
        PluginInstance.addListener(object : PluginListener(PluginInstance.miuiSystemUI(PLUGIN_LOCAL_MIUI_TILE)) {
            override fun onPluginBeforeLoad(plugin: Plugin, pluginContext: Context?, pluginInstance: PluginInstance) {
                dependHook(AdaptiveTileIconSize(pluginInstance, this))
            }
        })
    }

    open class TileState(instance: Any) : BaseReflectObject(instance) {
        constructor() : this(tileState.createInstance())

        var state: Int
            get() = instance.getIntField("state")
            set(value) = setStateValue(value)

        var icon: Any?
            get() = instance.getObjectFieldAs("icon")
            set(value) = instance.setObjectField("icon", value)

        var handlesLongClick: Boolean
            get() = instance.getBooleanField("handlesLongClick")
            set(value) = instance.setBooleanField("handlesLongClick", value)

        var label: CharSequence?
            get() = instance.getObjectFieldAs("label")
            set(value) = instance.setObjectField("label", value)

        var secondaryLabel: CharSequence?
            get() = instance.getObjectFieldAs("secondaryLabel")
            set(value) = instance.setObjectField("secondaryLabel", value)

        var contentDescription: CharSequence?
            get() = instance.getObjectFieldAs("contentDescription")
            set(value) = instance.setObjectField("contentDescription", value)

        var stateDescription: CharSequence?
            get() = instance.getObjectFieldAs("stateDescription")
            set(value) = instance.setObjectField("stateDescription", value)

        var expandedAccessibilityClassName: String?
            get() = instance.getStringField("expandedAccessibilityClassName")
            set(value) = instance.setStringField("expandedAccessibilityClassName", value)

        private fun setStateValue(value: Int) = instance.setIntField("state", value)

        fun copyFrom(from: Any): Boolean = from.callMethodAs("copyTo", instance)

        fun copyTo(to: Any): Boolean = instance.callMethodAs("copyTo", to)
    }

    open class TileAdapterState(instance: Any) : TileState(instance) {
        constructor() : this(tileAdapterState.createInstance())

        var value: Boolean
            get() = instance.getBooleanField("value")
            set(value) = instance.setBooleanField("value", value)
    }

    class TileBooleanState(instance: Any) : TileAdapterState(instance) {
        constructor() : this(tileBooleanState.createInstance())
    }

    object TileDrawableIcon {
        fun get(drawable: Drawable?): Any = if (drawable == null) {
            TileResourceIcon.empty()
        } else {
            tileDrawableIcon.createInstance(drawable)
        }
    }

    object TileResourceIcon {
        fun get(resId: Int): Any = tileResourceIcon.callStaticMethodAs("get", resId)

        fun empty(): Any = get(R.drawable.ic_empty)
    }
}
