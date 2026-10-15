package com.robco.pipboyradio.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.robco.pipboyradio.data.model.Station
import com.robco.pipboyradio.data.model.StreamMetadata

@Composable
fun ReceiverScreen(
    currentStation: Station?,
    isPlaying: Boolean,
    statusSignal: String,
    streamMetadata: StreamMetadata,
    nowPlayingText: String,
    volume: Float,
    isOverdrive: Boolean,
    onTogglePower: () -> Unit,
    onPrevStation: () -> Unit,
    onNextStation: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleOverdrive: () -> Unit,
    onOpenMetadata: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("receiver_screen"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top Terminal Header with Clock and Mode Switch
        TerminalHeader(
            isOverdrive = isOverdrive,
            onToggleOverdrive = onToggleOverdrive
        )

        // Hero Audio Spectrum & Frequency Visualizer — dynamically fills available space
        VisualizerCanvas(
            isPlaying = isPlaying,
            isOverdrive = isOverdrive,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        // Station Broadcast Telemetry & Live Now Playing Metadata
        StatusPanel(
            statusSignal = statusSignal,
            currentStation = currentStation,
            streamMetadata = streamMetadata,
            nowPlayingText = nowPlayingText,
            isPlaying = isPlaying,
            isOverdrive = isOverdrive,
            onOpenMetadata = onOpenMetadata
        )

        // Primary Playback Console (Tactile Power, Stepper & Volume)
        ControlsSection(
            isPlaying = isPlaying,
            volume = volume,
            isOverdrive = isOverdrive,
            onTogglePower = onTogglePower,
            onPrevStation = onPrevStation,
            onNextStation = onNextStation,
            onVolumeChange = onVolumeChange
        )
    }
}
