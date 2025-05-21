package com.xzakota.oshape.startup.rule.systemui.plugin

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import com.xzakota.android.extension.view.postThis
import com.xzakota.android.extension.view.toggleVisibility
import com.xzakota.android.hook.extension.afterHookedMethod
import com.xzakota.android.hook.extension.afterHookedMethodByName
import com.xzakota.android.hook.extension.beforeHookedMethodByName
import com.xzakota.android.hook.extension.chainGetObjectAs
import com.xzakota.android.hook.extension.chainGetObjectAsOrNull
import com.xzakota.android.hook.extension.reflect.createAfterHook
import com.xzakota.android.hook.extension.reflect.methodFinder
import com.xzakota.code.extension.callMethod
import com.xzakota.code.extension.callMethodAs
import com.xzakota.code.extension.getBooleanField
import com.xzakota.code.extension.getIntField
import com.xzakota.code.extension.getObjectField
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.code.util.MathUtils
import com.xzakota.oshape.startup.rule.systemui.api.PluginInstance
import com.xzakota.oshape.startup.rule.systemui.api.PluginListener
import kotlin.math.roundToInt

class VolumeSliderShowPercentage(
    pluginInstance: PluginInstance,
    listener: PluginListener
) : BaseSliderShowPercentage(pluginInstance, listener) {
    @SuppressLint("SetTextI18n")
    override fun onHookLoad() {
        super.onHookLoad()

        addHook(volumeExpandCollapsedAnimator.afterHookedMethod("calculateToViewValues") { param ->
            param.nonNullThisObject.getObjectFieldAs<List<*>?>("volumeColumnList")?.forEach {
                val superVolume = it?.getObjectFieldAs<TextView>("superVolume") ?: return@forEach
                initAndUpdateTopText(superVolume)
            }
        })

        addHook(volumePanelViewController.afterHookedMethod("updateSuperVolumeViewColor") { param ->
            param.nonNullThisObject.getObjectFieldAs<TextView>("mSuperVolume").typeface = Typeface.DEFAULT_BOLD
        })

        addHook(sliderController.afterHookedMethod("updateSliderValue", Int::class.java, Boolean::class.java) { param ->
            val controller = param.nonNullThisObject
            val isOriginalVolumeCallback = param.argAs<Boolean>(1)
            val slider = controller.callMethod("getSlider")
            if (slider != null && !isOriginalVolumeCallback) {
                val level = controller.callMethodAs<Int>("valueToVolume", param.args[0])
                val maxLevel = controller.getIntField("streamMaxVolume")
                val minLevel = controller.getIntField("streamMinVolume")
                val value = MathUtils.calIntPercentage(level, maxLevel, minLevel)
                controller.chainGetObjectAs<TextView?>("getSliderHolder().getTopText()")?.text = "$value%"
            }
        })

        addHook(sliderController.afterHookedMethodByName("syncSystemVolume") { param ->
            val controller = param.nonNullThisObject

            val systemVolume = controller.getObjectFieldAs<Int>("systemVolume") * 1000
            val sliderMaxValue = controller.getObjectFieldAs<Int>("sliderMaxValue")
            val sliderMinValue = controller.getObjectFieldAs<Int>("sliderMinValue")
            val value = MathUtils.calIntPercentage(systemVolume, sliderMaxValue, sliderMinValue)
            controller.chainGetObjectAsOrNull<TextView>("getSliderHolder().getTopText()")?.postThis {
                text = "$value%"
            }
        })

        addHook(volumePanelViewController.beforeHookedMethodByName("updateSuperVolumeView") { param ->
            val controller = param.nonNullThisObject
            val isExpanded = controller.getBooleanField("mExpanded")
            val superVolumeBg = controller.getObjectFieldAs<View>("mSuperVolumeBg")
            superVolumeBg.toggleVisibility(isExpanded)
        })

        addHook(
            volumePanelViewController.methodFinder()
                .filterByName("initColumn")
                .filterByParamCount(6)
                .first()
                .createAfterHook { param ->
                    val superVolume = param.argAs<Any>().getObjectFieldAs<TextView>("superVolume")

                    if (param.argAs(5)) {
                        return@createAfterHook
                    }

                    if (superVolume.visibility != View.VISIBLE) {
                        initAndUpdateTopText(superVolume)
                    }
                }
        )

        addHook(volumePanelViewController.afterHookedMethodByName("updateColumnIconBlendColor") { param ->
            updateTopTextBlendColor(param.argAs<Any>().getObjectFieldAs<TextView>("superVolume"))
        })

        addHook(volumePanelViewController.beforeHookedMethodByName("updateVolumeColumnH") { param ->
            val controller = param.nonNullThisObject
            val state = controller.getObjectField("mState") ?: return@beforeHookedMethodByName
            val isExpanded = controller.getBooleanField("mExpanded")
            val activeStream = controller.getIntField("mActiveStream")

            val volumeColumn = param.argAs<Any>()
            val stream = volumeColumn.getObjectFieldAs<Any>("stream")

            val streamState = state.getObjectFieldAs<Any>("states").callMethod("get", stream)
            val level = streamState?.getObjectFieldAs<Int>("level") ?: return@beforeHookedMethodByName
            val levelMax = streamState.getObjectFieldAs<Int>("levelMax")
            val value = (level * 1F / levelMax * 100).roundToInt()
            val columnSuperVolume = volumeColumn.getObjectFieldAs<TextView>("superVolume").also {
                it.text = "$value%"
                it.visibility = if (isExpanded) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }
            }

            if (isExpanded) {
                if (columnSuperVolume.typeface != Typeface.DEFAULT_BOLD) {
                    initAndUpdateTopText(columnSuperVolume)
                }
            } else {
                val isNeedShowDialog = controller.getBooleanField("mNeedShowDialog")
                if (!isNeedShowDialog) {
                    return@beforeHookedMethodByName
                }

                val superVolume = controller.getObjectFieldAs<TextView>("mSuperVolume")
                if (stream == activeStream) {
                    superVolume.text = "$value%"
                }
            }
        })
    }

    private fun initAndUpdateTopText(topTextView: TextView) {
        initTopText(topTextView)
        updateTopTextBlendColor(topTextView)
    }

    override fun getPanelControllerClassName(): String = "$VOLUME_PACKAGE_PREFIX.VolumePanelController"

    override fun getSliderControllerClassName(): String = "$VOLUME_PACKAGE_PREFIX.VolumeSliderController"

    override fun getPanelAnimatorClassName(): String = "$VOLUME_PACKAGE_PREFIX.VolumePanelAnimator"

    private val volumePanelViewController by lazy {
        loadClass("com.android.systemui.miui.volume.VolumePanelViewController")
    }

    private val volumeExpandCollapsedAnimator by lazy {
        loadClass("com.android.systemui.miui.volume.VolumeExpandCollapsedAnimator")
    }

    private companion object {
        const val VOLUME_PACKAGE_PREFIX = "miui.systemui.controlcenter.panel.main.volume"
    }
}
