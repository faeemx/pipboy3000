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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robco.pipboyradio.data.model.Station
import com.robco.pipboyradio.ui.theme.AccentAmber
import com.robco.pipboyradio.ui.theme.AccentRed
import com.robco.pipboyradio.ui.theme.PhosphorPrimary
import com.robco.pipboyradio.ui.theme.PhosphorSecondary
import com.robco.pipboyradio.ui.theme.PipBoyTypography
import com.robco.pipboyradio.ui.theme.SurfaceBorder
import com.robco.pipboyradio.ui.theme.SurfaceCard
import kotlin.math.sin

@Composable
fun MiniPlayerBar(
    station: Station?,
    isPlaying: Boolean,
    statusSignal: String,
    isOverdrive: Boolean,
    onTogglePower: () -> Unit,
    onNextStation: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isOverdrive) AccentRed else SurfaceBorder

    val infiniteTransition = rememberInfiniteTransition(label = "MiniWaveTransition")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "miniPhase"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(8.dp))
            .clickable { onExpand() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("mini_player_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Mini animated frequency waveform indicator
            Canvas(
                modifier = Modifier
                    .size(width = 28.dp, height = 20.dp)
                    .padding(end = 4.dp)
            ) {
                val barCount = 5
                val w = size.width / barCount
                for (i in 0 until barCount) {
                    val h = if (isPlaying) {
                        (sin(phase + i * 1.2f) * 0.4f + 0.6f) * size.height
                    } else {
                        3.dp.toPx()
                    }
                    drawRect(
                        color = if (isOverdrive) AccentRed else PhosphorPrimary,
                        topLeft = Offset(i * w + 1.dp.toPx(), size.height - h),
                        size = Size(w - 2.dp.toPx(), h)
                    )
                }
            }

            // Middle: Station Name & Status
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = station?.name ?: "--- NO SIGNAL ---",
                    style = PipBoyTypography.bodyLarge.copy(
                        color = if (isOverdrive) AccentRed else PhosphorPrimary,
                        fontSize = 12.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (isPlaying) "[ $statusSignal ]" else "[ STANDBY ]",
                    style = PipBoyTypography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = AccentAmber
                    )
                )
            }

            // Right: Play/Pause and Next step controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onTogglePower,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("mini_player_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = if (isOverdrive) AccentRed else PhosphorPrimary
                    )
                }

                IconButton(
                    onClick = onNextStation,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("mini_player_next_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Station",
                        tint = PhosphorSecondary
                    )
                }
            }
        }
    }
}
