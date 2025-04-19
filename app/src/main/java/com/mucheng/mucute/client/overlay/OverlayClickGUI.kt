package com.mucheng.mucute.client.overlay

import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import com.mucheng.mucute.client.game.ModuleCategory
import com.mucheng.mucute.client.game.ModuleContent
import com.mucheng.mucute.client.ui.component.NavigationRailX
import kotlin.math.sin

class OverlayClickGUI : OverlayWindow() {

    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags = flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            if (Build.VERSION.SDK_INT >= 31) {
                blurBehindRadius = 30
            }
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            dimAmount = 0.5f
            windowAnimations = android.R.style.Animation_Dialog
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    private var selectedModuleCategory by mutableStateOf(ModuleCategory.Combat)

    @Composable
    override fun Content() {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { OverlayManager.dismissOverlayWindow(this) },
            contentAlignment = Alignment.Center
        ) {
            Column(
                Modifier.wrapContentSize().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RainbowText("WClient", 32f)
                Spacer(modifier = Modifier.height(20.dp))

                ElevatedCard(
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxSize(0.9f)
                ) {
                    Row(Modifier.fillMaxSize()) {
                        NavigationRailX(
                            windowInsets = WindowInsets(0, 0, 0, 0)
                        ) {
                            ModuleCategory.entries.fastForEach { moduleCategory ->
                                NavigationRailItem(
                                    selected = selectedModuleCategory === moduleCategory,
                                    onClick = { selectedModuleCategory = moduleCategory },
                                    icon = {
                                        Icon(
                                            painterResource(moduleCategory.iconResId),
                                            contentDescription = null
                                        )
                                    },
                                    label = {
                                        Text(stringResource(moduleCategory.labelResId))
                                    },
                                    alwaysShowLabel = false
                                )
                            }
                        }
                        VerticalDivider()
                        AnimatedContent(
                            targetState = selectedModuleCategory,
                            label = "animatedPage",
                            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainer)
                        ) { moduleCategory ->
                            if (moduleCategory == ModuleCategory.Config) {
                                ConfigCategoryContent()
                            } else {
                                ModuleContent(moduleCategory)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun RainbowText(text: String, fontSize: Float) {
        val transition = rememberInfiniteTransition()
        val phase by transition.animateFloat(
            initialValue = 0f,
            targetValue = 2f * Math.PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3000, easing = LinearEasing)
            )
        )

        val colors = List(7) { i ->
            val hue = (i * 360 / 7 + (phase * 180 / Math.PI).toInt()) % 360
            Color.hsv(hue.toFloat(), 1f, 1f)
        }

        Text(
            text = text,
            style = TextStyle(
                fontSize = fontSize.sp, // Fixed: Convert Float to TextUnit.Sp
                brush = Brush.horizontalGradient(colors)
            )
        )
    }

    @Composable
    private fun ConfigCategoryContent() {
        // Configuration content placeholder
    }
}