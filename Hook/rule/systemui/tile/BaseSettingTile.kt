package com.xzakota.oshape.startup.rule.systemui.tile

import android.provider.Settings
import android.provider.Settings.NameValueTable
import android.widget.Switch
import androidx.annotation.EmptySuper
import com.xzakota.code.extension.callStaticMethodAs
import com.xzakota.code.extension.callStaticVoidMethod
import com.xzakota.oshape.extension.reInt
import com.xzakota.oshape.extension.toBoolean

@Suppress("LeakingThis")
abstract class BaseSettingTile(spec: String, private val storeName: String = spec) : BaseCustomTile(spec) {
    private val tableClass: Class<out NameValueTable>
    private var isEnabled = false

    init {
        tableClass = getTableClass()
    }

    override fun onClick() {
        updateSettings()
        refreshState(!isEnabled)
    }

    override fun onUpdateState(tmpState: TileManagement.TileState, value: Any?) {
        tmpState as TileManagement.TileBooleanState

        tmpState.icon = TileManagement.TileDrawableIcon.get(getThemeIcon(getIconName()))
        tmpState.label = customTile.getLabel()
        tmpState.expandedAccessibilityClassName = Switch::class.java.name

        if (value is Boolean) {
            if (tmpState.value == value) {
                return
            }

            tmpState.value = value

            if (isEnabled != tmpState.value) {
                onTileStateChange(tmpState.value)
            }
        } else {
            tmpState.value = isSettingsEnabled()
        }
        isEnabled = tmpState.value

        if (tmpState.value) {
            tmpState.state = TileManagement.STATE_ON
        } else {
            tmpState.state = TileManagement.STATE_OFF
        }
    }

    protected abstract fun getIconName(): String

    @EmptySuper
    protected open fun onTileStateChange(value: Boolean) {
    }

    protected open fun getTableClass(): Class<out NameValueTable> = Settings.System::class.java

    private fun isSettingsEnabled(): Boolean = runCatching {
        tableClass.callStaticMethodAs<Int>("getInt", context.contentResolver, storeName).toBoolean()
    }.getOrDefault(false)

    private fun updateSettings() = tableClass.callStaticVoidMethod(
        "putInt",
        context.contentResolver,
        storeName,
        isSettingsEnabled().reInt()
    )
}
