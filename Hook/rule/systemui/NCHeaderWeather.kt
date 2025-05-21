package com.xzakota.oshape.startup.rule.systemui

import android.content.Intent
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.IntDef
import androidx.core.view.isVisible
import cn.lyric.getter.api.data.LyricData
import com.xzakota.android.extension.content.getDimensionPixelSize
import com.xzakota.android.extension.content.getStyleIdByName
import com.xzakota.android.extension.view.parentGroup
import com.xzakota.android.hook.extension.afterHookedFirstConstructor
import com.xzakota.android.hook.extension.afterHookedMethod
import com.xzakota.android.hook.extension.afterHookedMethodByName
import com.xzakota.android.hook.extension.beforeHookedMethod
import com.xzakota.android.hook.extension.chainGetObjectAs
import com.xzakota.android.hook.extension.reflect.createBeforeHook
import com.xzakota.android.util.DensityUtils.dp2px
import com.xzakota.code.extension.getBooleanField
import com.xzakota.code.extension.getObjectFieldAs
import com.xzakota.code.extension.reflect.isStatic
import com.xzakota.oshape.startup.base.BaseHook
import com.xzakota.oshape.startup.rule.MusicHook
import com.xzakota.oshape.startup.rule.systemui.api.MiuiStub
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.combinedHeaderController
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.miuiNotificationHeaderView
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.setNotificationPanelVisible
import com.xzakota.oshape.startup.rule.systemui.shared.Notification.showLyric
import com.xzakota.oshape.util.DexKit
import com.xzakota.oshape.widget.WeatherView
import kotlinx.coroutines.flow.MutableStateFlow
import miui.sdk.isVerticalMode
import miuix.animation.Folme
import org.luckypray.dexkit.query.enums.StringMatchType
import java.lang.reflect.Method

class NCHeaderWeather : BaseHook(), MusicHook.MusicLyricListener {
    // 竖屏状态下的天气组件及动画
    private lateinit var vWeatherView: TextView
    private lateinit var vWeatherViewFolme: Any

    // 横屏状态下的天气组件及动画
    private var hWeatherView: TextView? = null
    private var hWeatherViewFolme: Any? = null

    private val isVerticalModeFlow = MutableStateFlow(false)

    override fun onHookLoad() {
        combinedHeaderController.afterHookedFirstConstructor { param ->
            val controller = param.nonNullThisObject
            val dateView = controller.getObjectFieldAs<View>("notificationDateTime")
            val landClock = controller.getObjectFieldAs<View>("notificationHorizontalTime")

            addWeatherViewAfterOf(dateView, ORIENTATION_PORTRAIT)
            if (isShowOnLandscape) {
                addWeatherViewAfterOf(landClock, ORIENTATION_LANDSCAPE)
            }

            // 创建动画
            vWeatherViewFolme = Folme.useAt(vWeatherView)
            hWeatherView?.let {
                hWeatherViewFolme = Folme.useAt(it)
            }
        }

        combinedHeaderController.afterHookedMethod("onSwitchProgressChanged", Float::class.java) { param ->
            val controller = param.nonNullThisObject
            val dateView = controller.getObjectFieldAs<View>("notificationDateTime")
            val landClock = controller.getObjectFieldAs<View>("notificationHorizontalTime")

            vWeatherView.translationX = dateView.translationX
            vWeatherView.translationY = dateView.translationY

            hWeatherView?.run {
                translationX = landClock.translationX
                translationY = landClock.translationY
            }
        }

        startFolmeAnimationAlpha.javaClass
        notificationHeaderExpandController.afterHookedFirstConstructor { param ->
            val controller = param.nonNullThisObject
            val callback = controller.getObjectFieldAs<Any>("notificationCallback")
            hookNotificationCallback(controller, callback::class.java)
        }

        // 更新资源
        updateResources()
        // 更新布局
        updateLayout()

        if (showLyric == 2) {
            setNotificationPanelVisible.createBeforeHook { param ->
                val controller = param.nonNullThisObject
                val isKeyguard = controller.chainGetObjectAs<Boolean>("keyguardStateController.mShowing")
                if (isKeyguard) {
                    return@createBeforeHook
                }

                val isVisible = controller.getBooleanField("visible")
                val setVisible = param.argAs<Boolean>()
                if (isVisible == setVisible) {
                    return@createBeforeHook
                }

                if (setVisible) {
                    onLyricUpdateImmediately(false)
                    MusicHook.addLyricListener(this)
                } else {
                    MusicHook.removeLyricListener(this)
                }
            }
        }
    }

    override fun onLyricUpdate(lyricData: LyricData) {
        onLyricUpdateImmediately(false)
    }

    override fun onLyricUpdateImmediately(isByController: Boolean) {
        if (MusicHook.isPlaying) {
            vWeatherView.isVisible = false
            hWeatherView?.isVisible = false
        } else {
            onLyricStop(null)
        }
    }

    override fun onLyricStop(lyricData: LyricData?) {
        val isVerticalMode = isVerticalModeFlow.value
        vWeatherView.isVisible = isVerticalMode
        hWeatherView?.isVisible = !isVerticalMode
    }

    override fun onMusicDestroy() {
        onLyricStop(null)
    }

    private fun hookNotificationCallback(expandController: Any, callbackClass: Class<*>) {
        callbackClass.afterHookedMethodByName("onAppearanceChanged") {
            val newAppearance = it.argAs<Boolean>()
            val animate = it.argAs<Boolean>(1)

            val startFolmeAnimationAlpha = { view: View?, folme: Any? ->
                if (view != null && folme != null) {
                    if (startFolmeAnimationAlpha.isStatic) {
                        val alpha = if (newAppearance) {
                            1F
                        } else {
                            0F
                        }

                        val args = if (startFolmeAnimationAlpha.parameterCount == 5) {
                            arrayOf(expandController, view, folme, alpha, animate)
                        } else {
                            arrayOf(view, folme, alpha, animate)
                        }

                        startFolmeAnimationAlpha.invoke(null, *args)
                    }
                }
            }

            startFolmeAnimationAlpha(vWeatherView, vWeatherViewFolme)
            startFolmeAnimationAlpha(hWeatherView, hWeatherViewFolme)
        }

        callbackClass.afterHookedMethodByName("onExpansionChanged") {
            val headerController = expandController.chainGetObjectAs<Any>("headerController.get()")
            headerController.getObjectFieldAs<View>("notificationDateTime").let {
                vWeatherView.translationX = it.translationX
                vWeatherView.translationY = it.translationY
            }

            hWeatherView?.run {
                headerController.getObjectFieldAs<View>("notificationHorizontalTime").let {
                    translationX = it.translationX
                    translationY = it.translationY
                }
            }
        }
    }

    private fun updateResources() {
        miuiNotificationHeaderView.afterHookedMethodByName("updateHeaderResources") { param ->
            val viewGroup = param.thisObjectAs<ViewGroup>()
            val orientation = viewGroup.getObjectFieldAs<Int>("mOrientation")
            if (orientation == -1) {
                return@afterHookedMethodByName
            }

            val dateView = viewGroup.getObjectFieldAs<TextView>("mDateView")
            val landClock = viewGroup.getObjectFieldAs<TextView>("mLandClock")

            vWeatherView.setTextSize(0, dateView.textSize)
            vWeatherView.setTextColor(dateView.textColors)
            vWeatherView.typeface = dateView.typeface

            hWeatherView?.run {
                setTextSize(0, landClock.textSize)
                setTextColor(landClock.textColors)
                typeface = landClock.typeface
            }
        }
    }

    private fun updateLayout() {
        miuiNotificationHeaderView.beforeHookedMethod("updateLayout") {
            val viewGroup = it.thisObjectAs<ViewGroup>()
            val context = viewGroup.context
            val configuration = context.resources.configuration
            val orientation = viewGroup.getObjectFieldAs<Int>("mOrientation")
            val screenLayout = viewGroup.getObjectFieldAs<Int>("mScreenLayout")

            if (orientation == configuration.orientation && screenLayout == configuration.screenLayout) {
                return@beforeHookedMethod
            }

            val isVerticalMode = isVerticalMode(context)
            isVerticalModeFlow.value = isVerticalMode

            if (MusicHook.isPlaying) {
                return@beforeHookedMethod
            }
            vWeatherView.isVisible = isVerticalMode
            hWeatherView?.isVisible = !isVerticalMode
        }
    }

    private fun addWeatherViewAfterOf(view: View, @Orientation key: Int) {
        val weatherView = WeatherView(view.context, isDisplayCity).apply {
            var appearance = "TextAppearance."
            when (key) {
                ORIENTATION_PORTRAIT -> {
                    vWeatherView = this
                    appearance += "QSControl.Date"
                }

                ORIENTATION_LANDSCAPE -> {
                    hWeatherView = this
                    appearance += "NSNotification.Clock"
                }
            }

            setTextAppearance(context.getStyleIdByName(appearance))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_VERTICAL
                marginStart = context.getDimensionPixelSize("notification_panel_time_date_space") + dp2px(5f)
            }

            setOnClickListener {
                runCatching {
                    MiuiStub.sysUIProvider.activityStarter.startActivity(
                        Intent().apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            component = WeatherView.COMPONENT_WEATHER
                        },
                        true
                    )
                }
            }
        }

        val viewParent = view.parentGroup ?: return
        viewParent.addView(weatherView, viewParent.indexOfChild(view) + 1)
    }

    @IntDef(value = [ORIENTATION_PORTRAIT, ORIENTATION_LANDSCAPE])
    @Retention(AnnotationRetention.SOURCE)
    private annotation class Orientation

    // 显示城市
    private val isDisplayCity by lazy {
        prefs.getBoolean("systemui_notification_center_add_weather_city_enabled")
    }

    // 横屏显示
    private val isShowOnLandscape by lazy {
        prefs.getBoolean("systemui_notification_center_weather_show_on_landscape_enabled")
    }

    private val notificationHeaderExpandController by lazy {
        loadClass("com.android.systemui.controlcenter.shade.NotificationHeaderExpandController")
    }

    private val startFolmeAnimationAlpha by lazy {
        DexKit.findMemberOrLog<Method>(this, "$name(startFolmeAnimationAlpha)") { bridge ->
            bridge.findMethod {
                matcher {
                    declaredClass(notificationHeaderExpandController)
                    name("startFolmeAnimationAlpha", StringMatchType.Contains)
                }
            }.singleOrNull()
        }
    }
}
