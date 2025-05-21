package com.xzakota.oshape.startup.rule.systemui.tile

import android.content.Intent
import android.provider.Settings
import com.xzakota.oshape.R

class SunlightModeTile : BaseSettingTile(makeCustomSpec(SPEC), SPEC) {
    override fun onCreateTile(): MiuiCustomTile = MiuiCustomTile(R.string.sunlight_mode)

    override fun getLongClickIntent(): Intent = Intent(Settings.ACTION_DISPLAY_SETTINGS)

    override fun getIconName(): String = SPEC

    private companion object {
        const val SPEC = "sunlight_mode"
    }
}
