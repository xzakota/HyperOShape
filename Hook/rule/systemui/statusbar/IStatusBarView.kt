package com.xzakota.oshape.startup.rule.systemui.statusbar

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.EmptySuper
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.repeatOnLifecycle
import com.xzakota.android.hook.core.provider.XProxyClassProvider
import com.xzakota.android.hook.startup.base.BaseRule
import com.xzakota.android.hook.startup.base.BaseShare
import com.xzakota.android.lifecycle.RepeatWhenAttached.repeatWhenAttached
import com.xzakota.code.extension.recoverAll
import com.xzakota.oshape.startup.rule.systemui.api.MiuiStub
import com.xzakota.oshape.startup.rule.systemui.shared.StatusBar.statusIconDisplayable
import com.xzakota.reflect.extension.callStaticMethod
import com.xzakota.reflect.extension.findStaticField
import com.xzakota.reflect.extension.typeInstanceOf
import com.xzakota.reflect.util.InvokeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import miui.extension.view.setMiSelfBlur
import net.bytebuddy.ByteBuddy
import net.bytebuddy.implementation.FieldAccessor
import net.bytebuddy.implementation.InvocationHandlerAdapter
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Modifier

interface IStatusBarView {
    fun getSlot(): String
    fun insertIndexOf(array: List<String>): Int = 0

    /**
     * 若使用非静态内部类, 请勿在 hook 内直接操作外部类对象
     *
     * @return 伴生 hook 规则
     */
    fun companionRule(): BaseRule? = null

    fun isBlockInControlCenter(): Boolean = false
    fun isBlockInRight(): Boolean = false
    fun isBlockInMiniRight(): Boolean = false
}

@Deprecated("R 资源获取异常")
abstract class IStaticStatusBarIcon(val iconResID: Int, var description: String = "") : IStatusBarView

abstract class IDisplayableStatusBarView : BaseShare(), IStatusBarView {
    lateinit var helper: StatusBarViewHelper
        private set

    protected val statusBarIconController = MiuiStub.myProvider.statusBarIconController

    protected var lightColor = 0
    protected var darkColor = 0
    protected var isUseTint = false

    protected val tintAreas = mutableListOf<Rect>()
    protected var darkIntensity = 0F
    protected var viewTint = 0

    @JvmField
    protected var isVisibleByController = false

    @JvmField
    protected var visibleState = VISIBLE_STATE_HIDDEN

    @JvmField
    protected var isBlocked = false

    fun construct(context: Context): FrameLayout = MADE_CLASS.typeInstanceOf<FrameLayout>(context, null).apply {
        tag = this@IDisplayableStatusBarView
        helper = StatusBarViewHelper(this)

        bind(this, context)
    }

    abstract fun bind(parent: FrameLayout, context: Context)

    fun isHelperInitialized(): Boolean = ::helper.isInitialized

    open fun setVisibleByController(isVisible: Boolean) {
        if (isVisibleByController != isVisible) {
            isVisibleByController = isVisible
            updateVisibility()
        }
    }

    open fun getVisibleState(): Int = visibleState
    open fun setVisibleState(state: Int) {
        setVisibleState(state, false)
    }

    open fun setVisibleState(state: Int, isUseAnim: Boolean) {
        if (helper.removeFlag || visibleState == state) {
            return
        }

        visibleState = state
        helper.view.isVisible = state != VISIBLE_STATE_HIDDEN
    }

    open fun isIconVisible(): Boolean = isVisibleByController && !isBlocked || helper.removeFlag

    open fun getDeemHide(): Boolean = false
    open fun setDeemHide(isDeemHide: Boolean) {}

    open fun isIconBlocked(): Boolean = false
    open fun setBlocked(isBlocked: Boolean) {
        if (this.isBlocked != isBlocked) {
            this.isBlocked = isBlocked
            updateVisibility()
        }
    }

    open fun isSignalView(): Boolean = false

    @EmptySuper
    open fun setDripEnd(isDripEnd: Boolean) {}

    open fun setAnimationEnable(isAnimationEnable: Boolean) {
        helper.isAnimateEnable = isAnimationEnable
    }

    @EmptySuper
    open fun setDecorColor(color: Int) {}

    @EmptySuper
    open fun setStaticDrawableColor(color: Int) {}

    open fun setStaticDrawableColor(color: Int, contrastColor: Int) {
        setStaticDrawableColor(color)
    }

    open fun getBlurRadius(): Int = helper.blurRadius
    open fun setBlurRadius(radius: Int) = helper.setViewBlurRadius(radius)

    open fun getRemoveFlag(): Boolean = helper.removeFlag
    open fun performRemove() {
        if (!helper.removeFlag) {
            return
        }

        helper.removeFlag = false
        helper.view.isVisible = false
    }

    open fun onDensityOrFontScaleChanged() = updateResources()
    open fun onUiModeChanged() = updateResources()
    open fun onMiuiThemeChanged() = updateResources()

    open fun onDarkChanged(tintAreas: List<Rect>, darkIntensity: Float, iconTint: Int) {
        this.tintAreas.recoverAll(tintAreas)
        this.darkIntensity = darkIntensity
        this.viewTint = iconTint

        updateLightDarkTint()
    }

    fun onDarkChanged(
        tintAreas: List<Rect>,
        darkIntensity: Float,
        iconTint: Int,
        lightColor: Int,
        darkColor: Int,
        isUseTint: Boolean
    ) {
        this.tintAreas.recoverAll(tintAreas)
        this.darkIntensity = darkIntensity
        this.viewTint = iconTint
        this.lightColor = lightColor
        this.darkColor = darkColor
        this.isUseTint = isUseTint

        updateLightDarkTint()
    }

    open fun onLightDarkTintChanged(lightColor: Int, darkColor: Int, isUseTint: Boolean) {
        this.lightColor = lightColor
        this.darkColor = darkColor
        this.isUseTint = isUseTint

        updateLightDarkTint()
    }

    @EmptySuper
    protected open fun updateLightDarkTint() {}

    @EmptySuper
    protected open fun updateResources() {}

    @EmptySuper
    protected open fun updateView() {}

    protected open fun updateVisibility() {
        val view = helper.view

        if (isVisibleByController && !isBlocked) {
            if (helper.removeFlag && view.isVisible) {
                visibleState = VISIBLE_STATE_HIDDEN
                updateView()
                helper.removeFlag = false
                view.requestLayout()
            } else {
                helper.removeFlag = false
                view.isVisible = true
                updateView()
            }
        } else {
            if (!view.isGone && helper.isAnimateEnable) {
                helper.removeFlag = true
                view.requestLayout()
            } else {
                helper.removeFlag = false
                view.isGone = true
            }
        }
    }

    companion object {
        const val VISIBLE_STATE_SHOWING = 0
        const val VISIBLE_STATE_HIDING = 1
        const val VISIBLE_STATE_HIDDEN = 2

        @Suppress("UNCHECKED_CAST")
        val MADE_CLASS by lazy {
            val className = "com.xzakota.systemui.DisplayableStatusBarIcon"
            val classMaker = ByteBuddy().subclass(FrameLayout::class.java)
                .name(className)
                .implement(statusIconDisplayable)
                .defineField("handler", InvocationHandler::class.java, Modifier.PRIVATE or Modifier.STATIC)
                .defineMethod("setHandler", Void.TYPE, Modifier.PUBLIC or Modifier.STATIC)
                .withParameter(InvocationHandler::class.java)
                .intercept(FieldAccessor.ofField("handler").setsArgumentAt(0))
                .method { it.isAbstract || it.isDefaultMethod }
                .intercept(InvocationHandlerAdapter.toField("handler"))
                .make()

            XProxyClassProvider.injectClass(classMaker.bytes, className)
            XProxyClassProvider.loadClass(className).also {
                it.callStaticMethod("setHandler", InvocationHandler { proxy, method, args ->
                    val tag = (proxy as View).tag as IDisplayableStatusBarView

                    return@InvocationHandler when (method.name) {
                        "getSlot" -> tag.getSlot()

                        "getVisibleState" -> tag.getVisibleState()
                        "setVisibleState" -> if (method.parameterCount == 1) {
                            tag.setVisibleState(args[0] as Int)
                        } else {
                            tag.setVisibleState(args[0] as Int, args[1] as Boolean)
                        }

                        "isIconVisible" -> tag.isIconVisible()

                        "getDeemHide" -> tag.getDeemHide()
                        "setDeemHide" -> tag.setDeemHide(args[0] as Boolean)

                        "isIconBlocked" -> tag.isIconBlocked()
                        "setBlocked" -> tag.setBlocked(args[0] as Boolean)

                        "isSignalView" -> tag.isSignalView()

                        "setDripEnd" -> tag.setDripEnd(args[0] as Boolean)
                        "setAnimationEnable" -> tag.setAnimationEnable(args[0] as Boolean)

                        "setDecorColor" -> tag.setDecorColor(args[0] as Int)
                        "setStaticDrawableColor" -> if (method.parameterCount == 1) {
                            tag.setStaticDrawableColor(args[0] as Int)
                        } else {
                            tag.setStaticDrawableColor(args[0] as Int, args[1] as Int)

                        }

                        "getBlurRadius" -> tag.getBlurRadius()
                        "setBlurRadius" -> tag.setBlurRadius(args[0] as Int)

                        "getRemoveFlag" -> tag.getRemoveFlag()
                        "performRemove" -> tag.performRemove()

                        "onDensityOrFontScaleChanged" -> tag.onDensityOrFontScaleChanged()
                        "onUiModeChanged" -> tag.onUiModeChanged()
                        "onDarkChanged" -> if (args.size == 6) {
                            tag.onDarkChanged(
                                args[0] as List<Rect>,
                                args[1] as Float,
                                args[2] as Int,
                                args[3] as Int,
                                args[4] as Int,
                                args[5] as Boolean
                            )
                        } else {
                            tag.onDarkChanged(args[0] as List<Rect>, args[1] as Float, args[2] as Int)
                        }

                        "onLightDarkTintChanged" -> tag.onLightDarkTintChanged(
                            args[0] as Int,
                            args[1] as Int,
                            args[2] as Boolean
                        )

                        else -> if (method.name.startsWith("onMiuiThemeChanged")) {
                            tag.onMiuiThemeChanged()
                        } else {
                        }
                    }
                })
            }
        }
    }
}

abstract class IModernStatusBarView : IDisplayableStatusBarView() {
    protected val viewModel = StatusBarViewModel()

    override fun setVisibleByController(isVisible: Boolean) {
        viewModel.onVisibleByControllerChanged(isVisible)
    }

    override fun getVisibleState(): Int = viewModel.visibleState.value

    override fun setVisibleState(state: Int, isUseAnim: Boolean) {
        if (state == viewModel.visibleState.value) {
            return
        }

        viewModel.onVisibilityStateChanged(state)
    }

    override fun isIconVisible(): Boolean = viewModel.isShouldIconBeVisible()

    override fun setBlocked(isBlocked: Boolean) = viewModel.onViewBlockedChanged(isBlocked)
}

class StatusBarViewHelper(val view: View) {
    var isAnimateEnable = false
    var removeFlag = false
    var blurRadius = 0
    // var isDeemHide = false

    fun setViewBlurRadius(radius: Int) {
        if (blurRadius != radius) {
            blurRadius = radius

            if (view.width != 0 && view.height != 0) {
                view.setMiSelfBlur(blurRadius, ANIMATOR_CONTROLLER.findStaticField<ArrayList<Point>>("blurPoint").get())
            }
        }
    }

    private companion object {
        private val ANIMATOR_CONTROLLER by lazy {
            InvokeUtils.getClass("com.android.systemui.statusbar.anim.MiuiStatusBarIconAnimatorController")
        }
    }
}

open class StatusBarViewModel : ViewModel() {
    private val _isVisible = MutableStateFlow(false)

    private val _isBlocked = MutableStateFlow(false)
    val isBlocked = _isBlocked.asStateFlow()

    private val _isVisibleByController = MutableStateFlow(false)
    val isVisibleByController = _isVisibleByController.asStateFlow()

    private val _visibleState = MutableStateFlow(IDisplayableStatusBarView.VISIBLE_STATE_HIDDEN)
    val visibleState = _visibleState.asStateFlow()

    // TODO: 待完善
    fun bind(layout: FrameLayout) {
        layout.repeatWhenAttached { view ->
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                onRepeatOnLifecycle()
            }
        }
    }

    @EmptySuper
    protected open suspend fun onRepeatOnLifecycle() {}

    fun isShouldIconBeVisible(): Boolean = _isVisible.value

    fun onVisibleByControllerChanged(isVisible: Boolean) {
        _isVisibleByController.value = isVisible
    }

    fun onViewBlockedChanged(isBlocked: Boolean) {
        _isBlocked.value = isBlocked
    }

    fun onVisibilityStateChanged(state: Int) {
        _visibleState.value = state
    }
}
