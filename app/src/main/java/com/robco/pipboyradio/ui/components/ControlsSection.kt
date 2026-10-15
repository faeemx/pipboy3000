package com.robco.pipboyradio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robco.pipboyradio.ui.theme.AccentAmber
import com.robco.pipboyradio.ui.theme.AccentRed
import com.robco.pipboyradio.ui.theme.PhosphorDim
import com.robco.pipboyradio.ui.theme.PhosphorPrimary
import com.robco.pipboyradio.ui.theme.PhosphorSecondary
import com.robco.pipboyradio.ui.theme.PipBoyTypography
import com.robco.pipboyradio.ui.theme.SurfaceBorder
import com.robco.pipboyradio.ui.theme.SurfaceCard
import kotlin.math.roundToInt

@Composable
fun ControlsSection(
    isPlaying: Boolean,
    volume: Float,
    isOverdrive: Boolean,
    onTogglePower: () -> Unit,
    onPrevStation: () -> Unit,
    onNextStation: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(10.dp))
            .padding(10.dp)
            .testTag("controls_section"),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Main Action Power & Step Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Station Button
            OutlinedButton(
                onClick = onPrevStation,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PhosphorPrimary),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder.copy(alpha = 0.6f))
                ),
                modifier = Modifier
                    .weight(0.9f)
                    .height(46.dp)
                    .testTag("prev_station_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Station",
                    tint = if (isOverdrive) AccentRed else PhosphorPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "PREV",
                    style = PipBoyTypography.labelSmall.copy(fontSize = 11.sp),
                    modifier = Modifier.padding(start = 2.dp)
                )
            }

            // Big Central Play / Pause Button
            Button(
                onClick = onTogglePower,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPlaying) {
                        if (isOverdrive) AccentRed else Color(0xFFC62828)
                    } else {
                        if (isOverdrive) AccentRed else PhosphorPrimary
                    },
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .weight(1.5f)
                    .height(46.dp)
                    .testTag("power_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = if (isPlaying) "PAUSE" else "INITIALIZE",
                        style = PipBoyTypography.titleMedium.copy(
                            color = Color.Black,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            // Next Station Button
            OutlinedButton(
                onClick = onNextStation,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PhosphorPrimary),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder.copy(alpha = 0.6f))
                ),
                modifier = Modifier
                    .weight(0.9f)
                    .height(46.dp)
                    .testTag("next_station_btn")
            ) {
                Text(
                    text = "NEXT",
                    style = PipBoyTypography.labelSmall.copy(fontSize = 11.sp),
                    modifier = Modifier.padding(end = 2.dp)
                )
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Station",
                    tint = if (isOverdrive) AccentRed else PhosphorPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Volume Slider Console
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = "Volume",
                tint = if (isOverdrive) AccentRed else PhosphorPrimary,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = "VOL",
                style = PipBoyTypography.labelSmall.copy(fontSize = 11.sp, color = PhosphorSecondary)
            )

            Slider(
                value = volume,
                onValueChange = onVolumeChange,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = if (isOverdrive) AccentRed else PhosphorPrimary,
                    activeTrackColor = if (isOverdrive) AccentRed else PhosphorPrimary,
                    inactiveTrackColor = PhosphorDim
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("volume_slider")
            )

            Text(
                text = "${(volume * 100).roundToInt()}%",
                style = PipBoyTypography.labelSmall.copy(color = AccentAmber, fontSize = 11.sp),
                modifier = Modifier.width(38.dp)
            )
        }
    }
}
