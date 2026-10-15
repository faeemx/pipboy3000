package com.robco.pipboyradio.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robco.pipboyradio.data.model.Station
import com.robco.pipboyradio.data.model.StreamMetadata
import com.robco.pipboyradio.ui.theme.AccentAmber
import com.robco.pipboyradio.ui.theme.AccentRed
import com.robco.pipboyradio.ui.theme.PhosphorPrimary
import com.robco.pipboyradio.ui.theme.PhosphorSecondary
import com.robco.pipboyradio.ui.theme.PipBoyTypography
import com.robco.pipboyradio.ui.theme.SurfaceBorder
import com.robco.pipboyradio.ui.theme.SurfaceCard

@Composable
fun StatusPanel(
    statusSignal: String,
    currentStation: Station?,
    streamMetadata: StreamMetadata,
    nowPlayingText: String,
    isPlaying: Boolean,
    isOverdrive: Boolean,
    onOpenMetadata: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BlinkTransition")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha"
    )

    val isAlert = statusSignal.contains("ERR") || statusSignal.contains("ACQUIRING")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("status_panel"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Top Row: Signal Badge & Quick Metadata Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status pill
            Box(
                modifier = Modifier
                    .border(
                        1.dp,
                        if (isAlert) AccentRed else if (isPlaying) PhosphorPrimary else PhosphorSecondary.copy(alpha = 0.5f),
                        RoundedCornerShape(4.dp)
                    )
                    .background(
                        if (isAlert) AccentRed.copy(alpha = 0.2f) else if (isPlaying) PhosphorSecondary.copy(alpha = 0.2f) else Color.Transparent,
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isPlaying) "● $statusSignal" else "■ STANDBY",
                    style = PipBoyTypography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = if (isAlert) AccentRed else if (isPlaying) PhosphorPrimary else PhosphorSecondary
                    ),
                    modifier = Modifier.alpha(if (isAlert) blinkAlpha else 1f)
                )
            }

            // Compact Metadata Icon Button
            IconButton(
                onClick = onOpenMetadata,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("status_open_metadata_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Metadata",
                    tint = if (isOverdrive) AccentRed else PhosphorPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Center Station Name (Big, bold, retro illuminated text)
        Text(
            text = currentStation?.name ?: "--- NO FREQUENCY LOCKED ---",
            style = PipBoyTypography.headlineLarge.copy(
                color = if (isOverdrive) AccentRed else PhosphorPrimary,
                fontSize = 18.sp,
                letterSpacing = 0.5.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("current_station_name")
        )

        // Bottom Row: Live Now Playing Metadata Line or Category & Dial
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (isPlaying) "♪" else ">",
                style = PipBoyTypography.labelSmall.copy(
                    color = if (isPlaying) AccentAmber else PhosphorSecondary,
                    fontSize = 11.sp
                )
            )

            val subtitleText = if (isPlaying) {
                if (streamMetadata.bitrate.isNotBlank()) {
                    "$nowPlayingText • ${streamMetadata.bitrate}"
                } else {
                    nowPlayingText
                }
            } else {
                val category = currentStation?.category?.uppercase()?.let { "[ $it ]" } ?: "[ READY ]"
                if (streamMetadata.frequencyDial.isNotBlank()) {
                    "$category • ${streamMetadata.frequencyDial}"
                } else {
                    category
                }
            }

            Text(
                text = subtitleText,
                style = PipBoyTypography.bodyMedium.copy(
                    fontSize = 11.sp,
                    color = if (isPlaying) Color(0xFFDDFFDD) else PhosphorSecondary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("now_playing_text")
            )
        }
    }
}
