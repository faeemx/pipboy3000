package com.robco.pipboyradio.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.robco.pipboyradio.ui.theme.AccentAmber
import com.robco.pipboyradio.ui.theme.AccentRed
import com.robco.pipboyradio.ui.theme.PhosphorPrimary
import com.robco.pipboyradio.ui.theme.PhosphorSecondary
import com.robco.pipboyradio.ui.theme.PipBoyTypography
import com.robco.pipboyradio.ui.theme.SurfaceBorder
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TerminalHeader(
    isOverdrive: Boolean,
    onToggleOverdrive: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf("00:00:00") }

    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
        while (true) {
            currentTime = sdf.format(Date())
            delay(1000)
        }
    }

    val primaryColor by animateColorAsState(
        targetValue = if (isOverdrive) AccentRed else PhosphorPrimary,
        label = "headerPrimaryColor"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("terminal_header"),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Main Branding & Tactical Mode Toggle Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PIP-BOY 3000",
                    style = PipBoyTypography.headlineLarge.copy(
                        color = primaryColor,
                        fontSize = 22.sp,
                        letterSpacing = 1.5.sp
                    )
                )
                Text(
                    text = "ROBCO INDUSTRIES // TERMLINK",
                    style = PipBoyTypography.labelSmall.copy(
                        color = PhosphorSecondary,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                )
            }

            // Tactical Mode Switch (Overdrive button)
            Box(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = if (isOverdrive) AccentRed else PhosphorSecondary,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .background(
                        color = if (isOverdrive) AccentRed.copy(alpha = 0.2f) else Color(0x221FA85B),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onToggleOverdrive() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("overdrive_toggle_btn"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isOverdrive) "⚡ OVR" else "● OVR",
                    style = PipBoyTypography.labelSmall.copy(
                        fontSize = 11.sp,
                        color = if (isOverdrive) AccentRed else PhosphorPrimary
                    )
                )
            }
        }

        // Clean Sub-header status bar: Signal State & Live Digital Clock
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "> COMM-LINK: ONLINE",
                style = PipBoyTypography.labelSmall.copy(
                    color = AccentAmber,
                    fontSize = 11.sp
                )
            )

            Text(
                text = "UTC $currentTime",
                style = PipBoyTypography.labelSmall.copy(
                    color = AccentAmber,
                    fontSize = 11.sp
                ),
                modifier = Modifier.testTag("clock_text")
            )
        }

        // Clean phosphor glowing divider (no overflowing dash text!)
        HorizontalDivider(
            thickness = 1.dp,
            color = if (isOverdrive) AccentRed.copy(alpha = 0.5f) else SurfaceBorder.copy(alpha = 0.4f),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
