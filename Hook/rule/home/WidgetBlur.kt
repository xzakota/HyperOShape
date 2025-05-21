package com.xzakota.oshape.startup.rule.home

import android.graphics.Rect
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import com.xzakota.android.extension.view.setRoundRect
import com.xzakota.android.hook.extension.beforeHookedMethod
import com.xzakota.android.hook.extension.beforeHookedMethodByName
import com.xzakota.android.hook.extension.chainGetObjectAs
import com.xzakota.android.util.DensityUtils.dp2px
import com.xzakota.code.extension.callMethodAs
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.home.api.BlurUtilities
import com.xzakota.oshape.startup.rule.home.shared.Blur.blurPoints
import com.xzakota.oshape.startup.rule.home.shared.Blur.blurUtilities
import com.xzakota.oshape.startup.rule.home.shared.Folder.folderIcon
import com.xzakota.oshape.startup.rule.home.shared.Folder.folderIcon1x1
import miui.extension.view.clearMiBackgroundBlendColor
import miui.extension.view.setMiBackgroundBlendColors
import miui.extension.view.setMiBackgroundBlurMode
import miui.extension.view.setMiBackgroundBlurRadius
import miui.extension.view.setMiViewBlurMode
import miui.extension.view.setPassWindowBlurEnabled

object WidgetBlur : BaseHook() {
    override fun onHookLoad() {
        blurUtilities.beforeHookedMethod("setWidgetBackgroundBlendColors", View::class.java) { param ->
            val points = blurPoints ?: return@beforeHookedMethod
            val view = param.argAs<View>()
            view.setMiBackgroundBlendColors(points)

            param.result = null
        }

        blurUtilities.beforeHookedMethod("setWidgetOrMaMlBlur", View::class.java) { param ->
            val view = param.argAs<View>()
            view.setMiViewBlurMode(1)
            view.setMiBackgroundBlurRadius(dp2px(50))
            BlurUtilities.setWidgetBackgroundBlendColors(view)

            param.result = null
        }

        folderIcon.beforeHookedMethod("setBlur") { param ->
            val points = blurPoints ?: return@beforeHookedMethod

            val folder = param.thisObjectAs<View>()
            val isFolderIcon1x1 = folderIcon1x1.isInstance(folder)
            if (BlurUtilities.isFolderBlurSupported(isFolderIcon1x1) && folder.isVisible) {
                val icon = folder.getObjectFieldAs<ImageView>("mIconImageView")
                val radius = BlurUtilities.getFolderIconBlurRoundRectRadius(folder.context, isFolderIcon1x1, icon)
                val mode = if (folder.callMethodAs<Boolean>("shouldUseAnimBlur")) {
                    10
                } else {
                    1
                }

                icon.clearMiBackgroundBlendColor()
                icon.setMiViewBlurMode(mode)
                icon.setMiBackgroundBlendColors(points)
                icon.setRoundRect(
                    radius.toFloat(),
                    icon.paddingLeft, icon.paddingTop,
                    icon.measuredWidth - icon.paddingRight, icon.measuredHeight - icon.paddingBottom,
                    false
                )
            }

            param.result = null
        }

        val dragViewClass = loadClass("com.miui.home.launcher.DragView")

        dragViewClass.beforeHookedMethodByName("setBlur") { param ->
            val points = blurPoints ?: return@beforeHookedMethodByName
            if (!BlurUtilities.isBlurSupported()) {
                param.result = null
                return@beforeHookedMethodByName
            }

            val folder = param.argAs<View>()
            if (folderIcon.isInstance(folder)) {
                val isFolderIcon1x1 = folderIcon1x1.isInstance(folder)
                if (BlurUtilities.isFolderBlurSupported(isFolderIcon1x1)) {
                    val dragView = param.thisObjectAs<View>()
                    val icon = folder.callMethodAs<ImageView>("getIconImageView")
                    val radius = BlurUtilities.getFolderIconBlurRoundRectRadius(dragView.context, isFolderIcon1x1, icon)
                    val rect = dragView.callMethodAs<Rect>("getBlurRoundRect", folder)

                    dragView.clearMiBackgroundBlendColor()
                    dragView.setMiBackgroundBlurMode(1)
                    dragView.setMiViewBlurMode(1)
                    dragView.setRoundRect(radius.toFloat(), rect, false)
                    if (param.argAs(1)) {
                        dragView.setPassWindowBlurEnabled(true)
                    }
                    dragView.setMiBackgroundBlurRadius(dp2px(50))
                    dragView.setMiBackgroundBlendColors(points)

                    param.result = null
                }
            }
        }

        dragViewClass.beforeHookedMethod("setWidgetBlur", View::class.java, Boolean::class.java) { param ->
            val view = param.argAs<View>()
            if (!BlurUtilities.isWidgetBlurSupported() || !BlurUtilities.supportWidgetBackgroundBlur(view)) {
                param.result = null
                return@beforeHookedMethod
            }

            val dragView = param.thisObjectAs<View>()
            val rect = dragView.callMethodAs<Rect>("getBlurRoundRect", view)
            dragView.setMiBackgroundBlurMode(0)
            dragView.setMiViewBlurMode(1)
            dragView.setRoundRect(view.callMethodAs("getCornerRadius"), rect, true)

            val dragViewContainer = dragView.chainGetObjectAs<View>("mLauncher.getDragViewContainer()")
            dragViewContainer.setMiBackgroundBlurMode(1)
            dragViewContainer.setMiViewBlurMode(0)
            if (param.argAs(1)) {
                dragViewContainer.setPassWindowBlurEnabled(true)
            }

            BlurUtilities.setWidgetBackgroundBlendColors(dragView)
        }
    }

    fun View.addHomeWidgetBlur() {
        val points = blurPoints ?: return

        clearMiBackgroundBlendColor()
        setMiViewBlurMode(1)
        setMiBackgroundBlurRadius(dp2px(50))

        setMiBackgroundBlendColors(points)
    }
}
