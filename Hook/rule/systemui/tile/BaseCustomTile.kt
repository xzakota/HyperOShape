package com.xzakota.oshape.startup.rule.systemui.tile

import android.content.Intent
import androidx.annotation.CallSuper
import androidx.annotation.EmptySuper
import com.xzakota.android.extension.app.collapsePanels
import com.xzakota.android.hook.startup.base.BaseShare
import com.xzakota.android.util.SystemUtils
import com.xzakota.code.extension.callMethod
import com.xzakota.code.extension.callMethodAs
import com.xzakota.code.extension.getIntField
import com.xzakota.oshape.application.AppConstant
import com.xzakota.oshape.startup.base.IThemeResource
import com.xzakota.oshape.startup.rule.systemui.tile.TileManagement.STATE_DISABLED

@Suppress("LeakingThis")
abstract class BaseCustomTile(val spec: String) : BaseShare(), IThemeResource {
    val customTile = onCreateTile()
    lateinit var state: TileManagement.TileState
        private set

    private var realTile: Any? = null

    protected val context get() = TileManagement.context
    protected val activityStarter get() = TileManagement.activityStarter

    protected abstract fun onCreateTile(): MiuiCustomTile

    fun onRealTileCreated(tile: Any) {
        if (realTile != null) {
            return
        }

        state = newTileState()
        state.state = STATE_DISABLED

        this.realTile = tile
    }

    @EmptySuper
    open fun onTileCreated() {
    }

    open fun isAvailable(): Boolean = true

    abstract fun onClick()

    open fun onLongClick(): Boolean = false

    open fun onSecondaryClick(): Boolean = false

    open fun getLongClickIntent(): Intent? = null

    open fun newTileState(): TileManagement.TileState = TileManagement.TileBooleanState()

    abstract fun onUpdateState(tmpState: TileManagement.TileState, value: Any?)

    open fun handleShowStateMessage(): Boolean = false

    open fun handleUserSwitch(): Boolean = false

    open fun getMetricsCategory(): Int = 0

    @CallSuper
    open fun onDestroy() {
        realTile = null
    }

    fun isTileReady(tile: Any? = realTile): Boolean = tile?.callMethodAs<Boolean>("isTileReady") == true

    fun getTileReady(tile: Any? = realTile): Int = tile?.getIntField("mReadyState") ?: 0

    protected fun refreshState() {
        realTile?.callMethod("refreshState")
    }

    protected fun refreshState(value: Any?) {
        realTile?.callMethod("refreshState", value)
    }

    protected fun showStateMessage(message: CharSequence) {
        realTile?.callMethod("showStateMessage", message)
    }

    protected fun collapseStatusBar() {
        context.runCatching {
            SystemUtils.statusBarManager.collapsePanels()
        }
    }

    protected companion object {
        @JvmStatic
        fun makeCustomSpec(spec: String): String = AppConstant.APP_NAME + "_$spec"
    }

    inner class MiuiCustomTile(private val labelResId: Int) {
        private var label: String? = null

        constructor(label: String) : this(0) {
            this.label = label
        }

        fun getLabel(): String? = if (labelResId != 0) {
            getHostString(labelResId)
        } else {
            label
        }
    }
}
