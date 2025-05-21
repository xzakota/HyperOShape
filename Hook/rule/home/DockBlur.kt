package com.xzakota.oshape.startup.rule.home

import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import com.xzakota.android.extension.view.setRoundRect
import com.xzakota.android.hook.extension.afterHookedMethod
import com.xzakota.android.hook.extension.afterHookedMethodByName
import com.xzakota.android.hook.extension.reflect.createAfterHook
import com.xzakota.android.util.DensityUtils.dp2px
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.home.WidgetBlur.addHomeWidgetBlur
import com.xzakota.oshape.startup.rule.home.shared.HomeShared.launcher
import com.xzakota.oshape.util.DexKit
import miui.extension.view.clearMiAllBlurAndBlendColor
import java.lang.reflect.Method
import java.util.function.Consumer

object DockBlur : BaseHook() {
    override fun onHookLoad() {
        var dockBlurView: View? = null

        launcher.afterHookedMethod("setupViews") {
            val hotSeats = it.nonNullThisObject.getObjectFieldAs<FrameLayout>("mHotSeats")
            dockBlurView = View(hotSeats.context).apply {
                doOnAttach {
                    addHomeWidgetBlur()
                }

                doOnDetach {
                    clearMiAllBlurAndBlendColor()
                }

                setRoundRect(dockRadius.toFloat())
            }

            hotSeats.addView(
                dockBlurView,
                FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dockHeight).apply {
                    gravity = Gravity.BOTTOM
                    setMargins(dockHorizontalMargin, 0, dockHorizontalMargin, dockBottomMargin)
                }
            )
        }

        launcher.afterHookedMethod("onDarkModeChanged") {
            dockBlurView?.addHomeWidgetBlur()
        }

        // 添加动画
        loadClass("com.miui.home.launcher.compat.UserPresentAnimationCompatComplex")
            .afterHookedMethodByName("operateAllPresentAnimationRelatedViews") {
                dockBlurView?.run {
                    @Suppress("UNCHECKED_CAST")
                    val consumer = it.argAs<Consumer<View>>()
                    consumer.accept(this)
                }
            }

        showAnimationLambda.createAfterHook {
            val view = it.argAs<View>(2)
            if (view == dockBlurView) {
                view.translationZ = 0F
            }
        }
    }

    // 高度
    private val dockHeight by lazy {
        dp2px(prefs.getInt("home_dock_mi_blur_bg_height", 95))
    }

    // 圆角
    private val dockRadius by lazy {
        dp2px(prefs.getInt("home_dock_mi_blur_bg_rounded_corners", 30))
    }

    // 水平间距
    private val dockHorizontalMargin by lazy {
        dp2px(prefs.getInt("home_dock_mi_blur_bg_horizontal_margin", 15)) -
            getHostDimensionPixelSize("hotseats_padding_side")
    }

    // 底部间距
    private val dockBottomMargin by lazy {
        dp2px(prefs.getInt("home_dock_mi_blur_bg_bottom_margin", 0))
    }

    private val showAnimationLambda by lazy {
        DexKit.findMemberOrLog<Method>(this, "$name(ShowAnimationLambda)") { bridge ->
            bridge.findMethod {
                matcher {
                    declaredClass("com.miui.home.launcher.compat.UserPresentAnimationCompatV12Phone")
                    addInvoke {
                        name = "conversionValueFrom3DTo2D"
                    }
                    addInvoke {
                        name = "setTranslationZ"
                    }
                }
            }.single()
        }
    }
}
