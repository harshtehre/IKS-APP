package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renders a lightweight, elegant ancient-meets-modern Indian geometric motif background
 * inspired by Sulba Sutras sacred geometry and astronomical star grids.
 */
@Composable
fun SacredGeometryBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.03f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "geometry_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepestVoid, MidnightNavy, DeepNavy)
                )
            )
            .drawBehind {
                val cx = size.width * 0.85f
                val cy = size.height * 0.15f
                val maxR = size.width * 0.6f

                // Draw subtle concentric circles (Vedic Mandala geometric rings)
                for (i in 1..4) {
                    drawCircle(
                        color = SaffronPrimary.copy(alpha = pulse * (0.8f / i)),
                        radius = maxR * (i * 0.25f),
                        center = Offset(cx, cy),
                        style = Stroke(width = 1.2f)
                    )
                }

                // Draw secondary geometric grid lines at bottom-left
                val bx = size.width * 0.15f
                val by = size.height * 0.85f
                for (i in 1..3) {
                    drawCircle(
                        color = TealAccent.copy(alpha = pulse * 0.7f),
                        radius = size.width * 0.45f * (i * 0.33f),
                        center = Offset(bx, by),
                        style = Stroke(width = 1f)
                    )
                }
            }
    ) {
        content()
    }
}

/**
 * Premium 2026 Glassmorphic Card with animated glowing gradient border,
 * inner specular reflection highlight, and spring press feedback.
 */
@Composable
fun PremiumGlassCard(
    modifier: Modifier = Modifier,
    borderGlow: Boolean = false,
    shape: RoundedCornerShape = RoundedCornerShape(22.dp),
    tonalElevation: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "card_press_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "border_shimmer")
    val borderOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "border_offset"
    )

    val borderBrush = if (borderGlow) {
        Brush.sweepGradient(
            colors = listOf(
                SaffronPrimary.copy(alpha = 0.8f),
                SurfaceCardBorder.copy(alpha = 0.3f),
                TealAccent.copy(alpha = 0.7f),
                SaffronLight.copy(alpha = 0.9f),
                SaffronPrimary.copy(alpha = 0.8f)
            ),
            center = Offset.Zero
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                SurfaceCardBorder.copy(alpha = 0.5f),
                SaffronPrimary.copy(alpha = 0.25f),
                SurfaceCardBorder.copy(alpha = 0.3f)
            )
        )
    }

    Surface(
        modifier = modifier
            .then(
                if (isPressed || scale != 1f) {
                    Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                } else Modifier
            )
            .clip(shape)
            .border(
                border = BorderStroke(if (borderGlow) 1.5.dp else 1.dp, borderBrush),
                shape = shape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onClick() }
                } else Modifier
            ),
        shape = shape,
        color = GlassSurface,
        tonalElevation = tonalElevation
    ) {
        // Subtle top specular edge highlight
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                SaffronLight.copy(alpha = if (borderGlow) 0.6f else 0.25f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(18.dp),
                content = content
            )
        }
    }
}
