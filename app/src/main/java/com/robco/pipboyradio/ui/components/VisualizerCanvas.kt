package com.robco.pipboyradio.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robco.pipboyradio.ui.theme.AccentAmber
import com.robco.pipboyradio.ui.theme.AccentRed
import com.robco.pipboyradio.ui.theme.PhosphorBright
import com.robco.pipboyradio.ui.theme.PhosphorPrimary
import com.robco.pipboyradio.ui.theme.PhosphorSecondary
import com.robco.pipboyradio.ui.theme.PipBoyTypography
import com.robco.pipboyradio.ui.theme.SurfaceBorder
import com.robco.pipboyradio.ui.theme.SurfaceCard
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class VisualizerStyle(val displayName: String) {
    VFD_SEGMENTED("VFD BARS"),
    CRT_OSCILLO("CRT BEAM"),
    DUAL_VU("DUAL VU"),
    RADAR_SONAR("RADAR")
}

@Composable
fun VisualizerCanvas(
    isPlaying: Boolean,
    isOverdrive: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedStyle by rememberSaveable { mutableStateOf(VisualizerStyle.VFD_SEGMENTED) }

    val infiniteTransition = rememberInfiniteTransition(label = "VisualizerTransition")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phaseAnimation"
    )

    val fastPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fastPulse"
    )

    val cycleNextStyle = {
        val styles = VisualizerStyle.values()
        val nextIndex = (selectedStyle.ordinal + 1) % styles.size
        selectedStyle = styles[nextIndex]
    }

    val borderColor = if (isOverdrive) AccentRed else SurfaceBorder

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(10.dp))
            .padding(10.dp)
            .testTag("visualizer_box"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Header with Title and Interactive Style Cycler
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "[ AUDIO VISUALIZER ]",
                style = PipBoyTypography.labelSmall.copy(
                    fontSize = 11.sp,
                    color = if (isOverdrive) AccentRed else PhosphorPrimary
                )
            )

            // Interactive Mode Cycler Pill
            Box(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = if (isOverdrive) AccentRed else PhosphorPrimary,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .background(
                        color = if (isOverdrive) AccentRed.copy(alpha = 0.25f) else PhosphorSecondary.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable { cycleNextStyle() }
                    .padding(horizontal = 6.dp, vertical = 3.dp)
                    .testTag("vis_style_cycler")
            ) {
                Text(
                    text = "[ MODE: ${selectedStyle.displayName} ↻ ]",
                    style = PipBoyTypography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = if (isOverdrive) AccentRed else PhosphorPrimary
                    )
                )
            }
        }

        // Dedicated CRT Screen Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF040705), RoundedCornerShape(6.dp))
                .border(1.dp, borderColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                .clickable { cycleNextStyle() }
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Background subtle CRT phosphor graticule grid lines
                drawCrtGraticule(width, height, isOverdrive)

                if (!isPlaying) {
                    // Standby Mode: Oscilloscope sine wave line with graticule
                    drawStandbyOscilloscope(width, height, phase, isOverdrive)
                } else {
                    // Active Playback: Render selected visualizer style
                    when (selectedStyle) {
                        VisualizerStyle.VFD_SEGMENTED -> {
                            drawVfdSegmentedBars(width, height, phase, fastPulse, isOverdrive)
                        }
                        VisualizerStyle.CRT_OSCILLO -> {
                            drawCrtOscilloBeams(width, height, phase, fastPulse, isOverdrive)
                        }
                        VisualizerStyle.DUAL_VU -> {
                            drawDualVuMeters(width, height, phase, fastPulse, isOverdrive)
                        }
                        VisualizerStyle.RADAR_SONAR -> {
                            drawRadarSonar(width, height, phase, fastPulse, isOverdrive)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// Style 1: VFD Segmented Bargraph (Vintage Illuminated Audio Equalizer)
// -------------------------------------------------------------------------
private fun DrawScope.drawVfdSegmentedBars(
    width: Float,
    height: Float,
    phase: Float,
    fastPulse: Float,
    isOverdrive: Boolean
) {
    val barCount = 24
    val spacing = 3.5f
    val barWidth = (width - spacing * (barCount - 1)) / barCount
    val totalSegments = 16
    val segmentGap = 2f
    val segmentHeight = (height - 18f - (totalSegments - 1) * segmentGap) / totalSegments

    for (i in 0 until barCount) {
        val normI = i.toFloat() / barCount
        val bass = sin(phase * 1.8f + normI * 2.5f) * 0.45f + 0.55f
        val mid = cos(phase * 2.6f - normI * 4.2f) * 0.35f + 0.5f
        val high = sin(phase * 3.8f + normI * 6.5f) * 0.25f + 0.35f
        val pulseKick = (sin(fastPulse + normI * 3f) * 0.15f).coerceAtLeast(0f)
        val raw = ((bass * 0.5f + mid * 0.32f + high * 0.18f + pulseKick) * (1.1f - normI * 0.25f)).coerceIn(0.08f, 0.98f)

        val activeSegments = (raw * totalSegments).toInt().coerceIn(1, totalSegments)
        val peakSegment = (activeSegments + 1).coerceAtMost(totalSegments - 1)

        val x = i * (barWidth + spacing)

        // Draw stacked discrete LED/VFD blocks from bottom up
        for (seg in 0 until totalSegments) {
            val segY = height - 16f - (seg + 1) * segmentHeight - seg * segmentGap

            val isLit = seg < activeSegments
            val isPeak = seg == peakSegment

            val segColor = when {
                isOverdrive -> {
                    if (isLit) Color(0xFFFF4C4C).copy(alpha = 0.85f)
                    else Color(0x18FF4C4C)
                }
                seg >= totalSegments - 2 -> { // Peak Overdrive (Red)
                    if (isLit) Color(0xFFFF4C4C)
                    else Color(0x1A441111)
                }
                seg >= totalSegments - 5 -> { // Caution Warning (Amber)
                    if (isLit) AccentAmber
                    else Color(0x1A443311)
                }
                else -> { // Standard Level (Phosphor Mint Green)
                    if (isLit) PhosphorPrimary.copy(alpha = 0.9f)
                    else Color(0x141FA85B)
                }
            }

            drawRect(
                color = segColor,
                topLeft = Offset(x, segY),
                size = Size(barWidth, segmentHeight)
            )

            // Draw floating peak hold block
            if (isPeak) {
                drawRect(
                    color = if (isOverdrive) Color(0xFFFFCCCC) else PhosphorBright,
                    topLeft = Offset(x, segY),
                    size = Size(barWidth, segmentHeight)
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// Style 2: CRT Multi-Harmonic Phosphor Oscilloscope (Beam Waveforms)
// -------------------------------------------------------------------------
private fun DrawScope.drawCrtOscilloBeams(
    width: Float,
    height: Float,
    phase: Float,
    fastPulse: Float,
    isOverdrive: Boolean
) {
    val centerY = height / 2f
    val points = 100
    val stepX = width / points

    // Layer 1: Fundamental Harmonic Wave (Main carrier)
    val pathMain = Path()
    // Layer 2: Treble Flutter Wave (Fine audio harmonic)
    val pathHarmonic = Path()

    for (i in 0..points) {
        val x = i * stepX
        val normX = x / width

        // Fundamental wave with audio modulated frequencies
        val y1 = centerY +
                sin(normX * 12f + phase * 2f) * (height * 0.28f) +
                cos(normX * 24f - phase * 3f) * (height * 0.12f) +
                sin(normX * 48f + fastPulse * 4f) * (height * 0.06f)

        // Flutter secondary wave
        val y2 = centerY +
                cos(normX * 16f - phase * 1.5f) * (height * 0.22f) +
                sin(normX * 36f + phase * 4f) * (height * 0.09f)

        if (i == 0) {
            pathMain.moveTo(x, y1)
            pathHarmonic.moveTo(x, y2)
        } else {
            pathMain.lineTo(x, y1)
            pathHarmonic.lineTo(x, y2)
        }
    }

    val beamColorMain = if (isOverdrive) AccentRed else PhosphorPrimary
    val beamColorHarmonic = if (isOverdrive) AccentAmber else PhosphorBright

    // Beam bloom halo (thick stroke with low alpha)
    drawPath(
        path = pathMain,
        color = beamColorMain.copy(alpha = 0.25f),
        style = Stroke(width = 8f, cap = StrokeCap.Round)
    )
    // Sharp core beam
    drawPath(
        path = pathMain,
        color = beamColorMain,
        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
    )

    // Secondary harmonic wave
    drawPath(
        path = pathHarmonic,
        color = beamColorHarmonic.copy(alpha = 0.55f),
        style = Stroke(width = 1.5f, cap = StrokeCap.Round)
    )

    // Center graticule zero-line
    drawLine(
        color = PhosphorSecondary.copy(alpha = 0.3f),
        start = Offset(0f, centerY),
        end = Offset(width, centerY),
        strokeWidth = 1f
    )
}

// -------------------------------------------------------------------------
// Style 3: Dual Analog Decibel VU Meters (Left / Right Gauges)
// -------------------------------------------------------------------------
private fun DrawScope.drawDualVuMeters(
    width: Float,
    height: Float,
    phase: Float,
    fastPulse: Float,
    isOverdrive: Boolean
) {
    val meterWidth = width / 2f
    val needleColor = if (isOverdrive) AccentRed else Color(0xFFFFB000)

    for (channel in 0..1) {
        val centerX = if (channel == 0) meterWidth * 0.5f else meterWidth * 1.5f
        val pivotY = height * 0.95f
        val radius = height * 0.72f

        // Meter background dial plate
        drawCircle(
            color = Color(0x181FA85B),
            center = Offset(centerX, pivotY),
            radius = radius
        )

        // Scale arc ticks
        val arcStartAngle = 210f
        val arcSweep = 120f
        val ticks = 11

        for (t in 0..ticks) {
            val angleDeg = arcStartAngle + (t.toFloat() / ticks) * arcSweep
            val angleRad = (angleDeg * PI / 180f).toFloat()

            val innerR = radius - 14f
            val outerR = radius - 4f
            val isOverZone = t >= ticks - 3

            val tickColor = if (isOverZone) AccentRed else PhosphorSecondary

            val start = Offset(centerX + cos(angleRad) * innerR, pivotY + sin(angleRad) * innerR)
            val end = Offset(centerX + cos(angleRad) * outerR, pivotY + sin(angleRad) * outerR)

            drawLine(color = tickColor, start = start, end = end, strokeWidth = if (t % 3 == 0) 2f else 1f)
        }

        // Animated needle angle
        val audioEnergy = if (channel == 0) {
            sin(phase * 2.2f) * 0.35f + cos(fastPulse * 3f) * 0.25f + 0.45f
        } else {
            cos(phase * 1.9f) * 0.35f + sin(fastPulse * 3.2f) * 0.25f + 0.45f
        }.coerceIn(0.05f, 0.95f)

        val needleAngleDeg = arcStartAngle + audioEnergy * arcSweep
        val needleAngleRad = (needleAngleDeg * PI / 180f).toFloat()

        val needleEnd = Offset(
            centerX + cos(needleAngleRad) * (radius - 6f),
            pivotY + sin(needleAngleRad) * (radius - 6f)
        )

        // Needle
        drawLine(
            color = needleColor,
            start = Offset(centerX, pivotY),
            end = needleEnd,
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )

        // Pivot center cap
        drawCircle(
            color = PhosphorPrimary,
            center = Offset(centerX, pivotY),
            radius = 5f
        )
    }
}

// -------------------------------------------------------------------------
// Style 4: Acoustic Sonar Sweep (Radial Scope)
// -------------------------------------------------------------------------
private fun DrawScope.drawRadarSonar(
    width: Float,
    height: Float,
    phase: Float,
    fastPulse: Float,
    isOverdrive: Boolean
) {
    val center = Offset(width / 2f, height / 2f)
    val maxRadius = (height / 2f) - 6f

    // Concentric decibel rings
    val ringCount = 4
    for (r in 1..ringCount) {
        val ringR = maxRadius * (r.toFloat() / ringCount)
        drawCircle(
            color = PhosphorSecondary.copy(alpha = 0.25f),
            center = center,
            radius = ringR,
            style = Stroke(width = 1f)
        )
    }

    // Crosshair axes
    drawLine(
        color = PhosphorSecondary.copy(alpha = 0.3f),
        start = Offset(center.x - maxRadius, center.y),
        end = Offset(center.x + maxRadius, center.y),
        strokeWidth = 1f
    )
    drawLine(
        color = PhosphorSecondary.copy(alpha = 0.3f),
        start = Offset(center.x, center.y - maxRadius),
        end = Offset(center.x, center.y + maxRadius),
        strokeWidth = 1f
    )

    // Rotating Radar Sweep Line
    val sweepAngle = phase * 2f
    val sweepEnd = Offset(
        center.x + cos(sweepAngle) * maxRadius,
        center.y + sin(sweepAngle) * maxRadius
    )

    drawLine(
        color = if (isOverdrive) AccentRed else PhosphorBright,
        start = center,
        end = sweepEnd,
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )

    // Pulsing acoustic blips
    val blipCount = 6
    for (b in 0 until blipCount) {
        val blipAngle = (b * 60f * PI / 180f).toFloat() + phase * 0.5f
        val blipDistance = (sin(fastPulse + b) * 0.3f + 0.55f) * maxRadius
        val blipPos = Offset(center.x + cos(blipAngle) * blipDistance, center.y + sin(blipAngle) * blipDistance)

        drawCircle(
            color = if (isOverdrive) AccentRed else AccentAmber,
            center = blipPos,
            radius = 3.5f
        )
    }
}

// -------------------------------------------------------------------------
// Graticule & Standby Oscilloscope
// -------------------------------------------------------------------------
private fun DrawScope.drawCrtGraticule(width: Float, height: Float, isOverdrive: Boolean) {
    val gridStep = 24.dp.toPx()
    var currentY = gridStep
    while (currentY < height) {
        drawLine(
            color = if (isOverdrive) Color(0x18FF4C4C) else Color(0x121FA85B),
            start = Offset(0f, currentY),
            end = Offset(width, currentY),
            strokeWidth = 1f
        )
        currentY += gridStep
    }
}

private fun DrawScope.drawStandbyOscilloscope(
    width: Float,
    height: Float,
    phase: Float,
    isOverdrive: Boolean
) {
    val points = 60
    val stepX = width / points
    var lastX = 0f
    var lastY = height / 2f

    for (i in 0..points) {
        val x = i * stepX
        val y = height / 2f + sin(x * 0.04f + phase) * (height * 0.12f)
        if (i > 0) {
            drawLine(
                color = if (isOverdrive) AccentRed else PhosphorSecondary,
                start = Offset(lastX, lastY),
                end = Offset(x, y),
                strokeWidth = 2.5f
            )
        }
        lastX = x
        lastY = y
    }
}
