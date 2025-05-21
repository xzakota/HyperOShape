package com.xzakota.oshape.startup.rule.systemui.tile

import android.content.Intent
import android.net.Uri
import android.telephony.TelephonyManager
import android.widget.Switch
import com.xzakota.android.hook.XHookHelper
import io.github.libxposed.api.XposedModule
import androidx.core.net.toUri

class LSPosedTile : BaseCustomTile(makeCustomSpec(SPEC)) {
    override fun onCreateTile(): MiuiCustomTile = MiuiCustomTile(LABEL)

    override fun isAvailable(): Boolean = XHookHelper.module is XposedModule

    override fun onClick() {
        context.sendBroadcast(
            Intent(TelephonyManager.ACTION_SECRET_CODE).apply {
                setData("android_secret_code://5776733".toUri())
            }
        )
        collapseStatusBar()
    }

    override fun getMetricsCategory(): Int = -1

    override fun onUpdateState(tmpState: TileManagement.TileState, value: Any?) {
        tmpState as TileManagement.TileBooleanState

        tmpState.state = TileManagement.STATE_OFF
        tmpState.icon = TileManagement.TileDrawableIcon.get(getThemeIcon("lsposed"))
        tmpState.label = LABEL
        tmpState.contentDescription = LABEL
        tmpState.expandedAccessibilityClassName = Switch::class.java.name
    }

    private companion object {
        const val LABEL = "LSPosed"
        const val SPEC = "lsposed"
    }
}
