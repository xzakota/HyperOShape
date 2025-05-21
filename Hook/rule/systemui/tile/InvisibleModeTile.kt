package com.xzakota.oshape.startup.rule.systemui.tile

import android.content.Intent
import android.provider.Settings
import com.xzakota.android.extension.content.pm.versionCodeX
import com.xzakota.android.util.PropUtils
import com.xzakota.android.util.SystemUtils
import com.xzakota.android.util.SystemUtils.isMoreAndroidVersion
import com.xzakota.android.util.shell.ShellUtils
import com.xzakota.oshape.R
import com.xzakota.oshape.extension.toInt
import miui.sdk.isDevice
import miui.sdk.isInternationalBuild
import miui.sdk.isStableVersion
import miui.util.FeatureParser
import java.io.File

class InvisibleModeTile : BaseSettingTile(makeCustomSpec(SPEC), STORE_NAME) {
    override fun onCreateTile(): MiuiCustomTile = MiuiCustomTile(R.string.invisible_mode)

    override fun isAvailable(): Boolean = isSupportInvisibleMode

    override fun getLongClickIntent(): Intent = Intent().apply {
        setClassName("com.miui.securitycenter", "com.miui.permcenter.settings.InvisibleModeActivity")
    }

    override fun onTileStateChange(value: Boolean) {
        PropUtils.safeSetProp("persist.sys.invisible_mode", value.toInt().toString())
        ShellUtils.safeExecCmd(
            """
            am startservice com.miui.securitycenter/com.miui.permcenter.service.InvisibleModeService --ez RESTRICTION_NOW true
            """.trimIndent()
        )
    }

    override fun getTableClass(): Class<out Settings.NameValueTable> = Settings.Secure::class.java

    override fun getIconName(): String = SPEC

    private companion object {
        const val SPEC = "invisible_mode"
        const val STORE_NAME = "key_invisible_mode_state"

        val isSupportInvisibleMode = !isInternationalBuild() &&
            (isStableVersion() && isDevice("cetus") || securityVersion() >= 170) && isFeatureEnabled()

        fun securityVersion() = SystemUtils.packageManager.getPackageInfo("com.lbe.security.miui", 0)?.versionCodeX ?: 0

        fun isFeatureEnabled(): Boolean = if (isStableVersion() && isMoreAndroidVersion(30) &&
            "qcom" == FeatureParser.getString("vendor") && File("system/lib64/libmediastub.so").exists()
        ) {
            File("system_ext/lib64/libmediaimpl.so").exists()
        } else {
            true
        }
    }
}
