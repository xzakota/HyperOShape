package com.xzakota.oshape.startup.rule.settings

import com.xzakota.android.hook.extension.beforeHookedMethod
import com.xzakota.code.extension.callMethod
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.code.extension.setFloatField
import com.xzakota.oshape.model.entity.MiBgEffect
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.base.getThemeNightColor
import com.xzakota.oshape.startup.base.getThemeNormalColor

object ChangeDeviceBgEffect : BaseHook() {
    override fun onHookLoad() {
        val normalEffect = getBgEffect(false, false)
        val nightEffect = getBgEffect(true, false)
        val normalPadEffect = getBgEffect(false, true)
        val nightPadEffect = getBgEffect(true, true)

        hookSetEffectMethod("setPhoneLight", normalEffect)
        hookSetEffectMethod("setPhoneDark", nightEffect)
        hookSetEffectMethod("setPadLight", normalPadEffect ?: normalEffect)
        hookSetEffectMethod("setPadDark", nightPadEffect ?: nightEffect)
    }

    private fun hookSetEffectMethod(methodName: String, effect: MiBgEffect?) {
        if (effect == null) {
            return
        }

        loadClass("com.android.settings.device.BgEffectPainter").beforeHookedMethod(
            methodName, FloatArray::class.java
        ) { param ->
            val painter = param.nonNullThisObject
            painter.callMethod("setLightOffset", effect.lightOffset)
            painter.callMethod("setSaturateOffset", effect.saturateOffset)
            painter.callMethod("setPoints", effect.point)
            painter.callMethod("setColors", effect.colorFloatArray)
            painter.callMethod("setBound", param.args[0])

            updateMaterials(painter, effect)

            param.result = null
        }
    }

    private fun updateMaterials(painter: Any, effect: MiBgEffect) {
        val bgRuntimeShader = painter.getObjectFieldAs<Any>("mBgRuntimeShader")
        setFiled(painter, bgRuntimeShader, "uTranslateY", effect.translateY, 0F)
        setFiled(painter, bgRuntimeShader, "uAlphaMulti", effect.alphaMulti, 1F)
        setFiled(painter, bgRuntimeShader, "uAlphaOffset", effect.alphaOffset, 0.5F)
        setFiled(painter, bgRuntimeShader, "uNoiseScale", effect.noiseScale, 1.5F)
        setFiled(painter, bgRuntimeShader, "uPointOffset", effect.pointOffset, 0.1F)
        setFiled(painter, bgRuntimeShader, "uPointRadiusMulti", effect.pointRadiusMulti, 1.0F)
        setFiled(painter, bgRuntimeShader, "uShadowOffset", effect.shadowOffset, 0.01F)
        setFiled(painter, bgRuntimeShader, "uShadowNoiseScale", effect.shadowNoiseScale, 5.0F)
        setFiled(painter, bgRuntimeShader, "uShadowColorMulti", effect.shadowColorMulti, 0.3F)
        setFiled(painter, bgRuntimeShader, "uShadowColorOffset", effect.shadowColorOffset, 0.3F)
    }

    private fun setFiled(painter: Any, bgRuntimeShader: Any, filedName: String, value: Float, defaultValue: Float) {
        if (value == defaultValue) {
            return
        }

        painter.setFloatField(filedName, value)
        bgRuntimeShader.callMethod("setFloatUniform", filedName, value)
    }

    private fun getBgEffect(isDarkTheme: Boolean, isPad: Boolean): MiBgEffect? {
        var key = "settings_bg_effect"
        if (isPad) {
            key += "_pad"
        }

        return if (isDarkTheme) {
            getThemeNightColor(key)
        } else {
            getThemeNormalColor(key)
        }
    }
}
