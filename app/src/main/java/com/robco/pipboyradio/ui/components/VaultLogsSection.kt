package com.robco.pipboyradio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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

@Composable
fun VaultLogsSection(
    isOverdrive: Boolean,
    isRepoLoading: Boolean = false,
    repoStatusMessage: String? = null,
    totalStationCount: Int = 0,
    showRepoModal: Boolean = false,
    // Settings toggles & preferences
    autoPlayNextOnError: Boolean = true,
    dataSaverBuffer: Boolean = false,
    scanlinesEnabled: Boolean = true,
    pipBoyColorTheme: String = "GREEN",
    visualizerStyleName: String = "VFD BARS",
    onToggleOverdrive: () -> Unit = {},
    onToggleAutoPlayNext: () -> Unit = {},
    onToggleDataSaver: () -> Unit = {},
    onToggleScanlines: () -> Unit = {},
    onSelectColorTheme: (String) -> Unit = {},
    onSelectVisualizerStyle: (String) -> Unit = {},
    onOpenAddStationDialog: () -> Unit = {},
    onOpenRepoModal: () -> Unit = {},
    onCloseRepoModal: () -> Unit = {},
    onRemoveStation: (Station) -> Unit = {},
    onLoadRawJsonUrl: (url: String, append: Boolean) -> Unit = { _, _ -> },
    onLoadBundledStations: (append: Boolean) -> Unit = {},
    onRestoreDefaults: () -> Unit = {},
    onClearAllStations: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val borderColor = if (isOverdrive) AccentRed else SurfaceBorder

    var showPurgeConfirmDialog by rememberSaveable { mutableStateOf(false) }

    // Repository Manager Popup Dialog
    RepoManagerDialog(
        isOpen = showRepoModal,
        isOverdrive = isOverdrive,
        totalStationCount = totalStationCount,
        isLoading = isRepoLoading,
        onDismiss = onCloseRepoModal,
        onLoadRawJsonUrl = onLoadRawJsonUrl,
        onLoadBundledStations = onLoadBundledStations,
        onClearAllStations = onClearAllStations
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 4.dp)
            .testTag("vault_settings_section"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = if (isOverdrive) AccentRed else PhosphorPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "PIP-BOY 3000 // SYSTEM SETTINGS",
                    style = PipBoyTypography.titleMedium.copy(
                        color = if (isOverdrive) AccentRed else PhosphorPrimary,
                        fontSize = 13.sp
                    )
                )
            }

            Text(
                text = "CONFIG v2.0",
                style = PipBoyTypography.labelSmall.copy(fontSize = 10.sp, color = AccentAmber)
            )
        }

        // Card 1: Audio & Radio Playback Preferences (Toggles)
        SettingsCard(
            title = "AUDIO & TRANSMISSION PREFERENCES",
            borderColor = borderColor,
            icon = Icons.Default.VolumeUp
        ) {
            // Overdrive Boost Toggle
            SettingToggleRow(
                title = "OVERDRIVE AUDIO BOOST (+6dB)",
                subtitle = "Amplifies weak low-frequency signals across wasteland airwaves",
                isChecked = isOverdrive,
                onCheckedChange = { onToggleOverdrive() },
                isOverdrive = isOverdrive,
                testTag = "setting_toggle_overdrive"
            )

            // Auto-advance on stream error
            SettingToggleRow(
                title = "AUTO-ADVANCE ON SIGNAL LOSS",
                subtitle = "Automatically tunes to next frequency when radio stream drops",
                isChecked = autoPlayNextOnError,
                onCheckedChange = { onToggleAutoPlayNext() },
                isOverdrive = isOverdrive,
                testTag = "setting_toggle_auto_advance"
            )

            // Data Saver / Low-Bandwidth Buffer
            SettingToggleRow(
                title = "DATA-SAVER LOW LATENCY BUFFER",
                subtitle = "Minimizes network buffer size for unstable wireless relays",
                isChecked = dataSaverBuffer,
                onCheckedChange = { onToggleDataSaver() },
                isOverdrive = isOverdrive,
                testTag = "setting_toggle_data_saver"
            )
        }

        // Card 2: CRT Display & Phosphor Visuals
        SettingsCard(
            title = "CRT DISPLAY & VISUAL THEMES",
            borderColor = borderColor,
            icon = Icons.Default.DisplaySettings
        ) {
            // CRT Scanlines Effect Toggle
            SettingToggleRow(
                title = "CRT SCANLINES & PHOSPHOR GLOW",
                subtitle = "Simulates authentic vacuum tube phosphor persistence",
                isChecked = scanlinesEnabled,
                onCheckedChange = { onToggleScanlines() },
                isOverdrive = isOverdrive,
                testTag = "setting_toggle_scanlines"
            )

            // Pip-Boy Color Theme Palette
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "> CRT PHOSPHOR PALETTE:",
                    style = PipBoyTypography.labelSmall.copy(fontSize = 10.sp, color = PhosphorSecondary)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "GREEN" to "CLASSIC GREEN",
                        "AMBER" to "NEW VEGAS",
                        "CYAN" to "VAULT-TEC",
                        "WHITE" to "MONOCHROME"
                    ).forEach { (themeKey, label) ->
                        val isSelected = pipBoyColorTheme == themeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) {
                                        if (isOverdrive) AccentRed else PhosphorPrimary
                                    } else {
                                        SurfaceBorder.copy(alpha = 0.35f)
                                    },
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .background(
                                    if (isSelected) PhosphorSecondary.copy(alpha = 0.25f) else Color.Transparent,
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable { onSelectColorTheme(themeKey) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = PipBoyTypography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    color = if (isSelected) PhosphorPrimary else PhosphorSecondary
                                )
                            )
                        }
                    }
                }
            }

            // Visualizer Default Style
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "> DEFAULT AUDIO VISUALIZER:",
                    style = PipBoyTypography.labelSmall.copy(fontSize = 10.sp, color = PhosphorSecondary)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("VFD BARS", "CRT BEAM", "DUAL VU", "RADAR").forEach { style ->
                        val isSelected = visualizerStyleName == style
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) PhosphorPrimary else SurfaceBorder.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .background(
                                    if (isSelected) PhosphorSecondary.copy(alpha = 0.25f) else Color.Transparent,
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable { onSelectVisualizerStyle(style) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = style,
                                style = PipBoyTypography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    color = if (isSelected) PhosphorPrimary else PhosphorSecondary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Card 3: Station Library Management (Add, Repo, Restore, Purge)
        SettingsCard(
            title = "STATION LIBRARY MANAGEMENT",
            borderColor = borderColor,
            icon = Icons.Default.Radio
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "> ACTIVE TUNER STATIONS:",
                    style = PipBoyTypography.labelSmall.copy(fontSize = 11.sp, color = PhosphorSecondary)
                )

                Text(
                    text = if (totalStationCount == 0) "[ EMPTY LIBRARY ]" else "[ $totalStationCount STATIONS LOADED ]",
                    style = PipBoyTypography.labelSmall.copy(
                        fontSize = 11.sp,
                        color = if (totalStationCount == 0) AccentAmber else PhosphorPrimary
                    )
                )
            }

            if (repoStatusMessage != null) {
                Text(
                    text = "> $repoStatusMessage",
                    style = PipBoyTypography.bodyMedium.copy(
                        fontSize = 10.sp,
                        color = if (repoStatusMessage.startsWith("ERR")) AccentRed else PhosphorSecondary
                    ),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            // Row 1: Direct Add Station & Manage Repositories
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "+ ADD CUSTOM STATION"
                Button(
                    onClick = onOpenAddStationDialog,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOverdrive) AccentRed else PhosphorPrimary,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("settings_add_station_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "ADD STATION",
                        style = PipBoyTypography.labelSmall.copy(fontSize = 11.sp, color = Color.Black),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // "MANAGE REPOSITORIES"
                OutlinedButton(
                    onClick = onOpenRepoModal,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PhosphorPrimary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("settings_manage_repos_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderZip,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "REPOSITORIES",
                        style = PipBoyTypography.labelSmall.copy(fontSize = 10.sp, color = PhosphorPrimary),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // Row 2: Restore Factory Defaults & Purge All
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // RESTORE DEFAULTS
                OutlinedButton(
                    onClick = onRestoreDefaults,
                    enabled = !isRepoLoading,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentAmber),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(AccentAmber.copy(alpha = 0.6f))
                    ),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(38.dp)
                        .testTag("restore_defaults_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = AccentAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "RESTORE 275+ DEFAULTS",
                        style = PipBoyTypography.labelSmall.copy(fontSize = 9.sp, color = AccentAmber),
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }

                // PURGE ALL
                Button(
                    onClick = onClearAllStations,
                    enabled = totalStationCount > 0 && !isRepoLoading,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentRed,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .weight(0.8f)
                        .height(38.dp)
                        .testTag("settings_purge_all_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "PURGE ALL",
                        style = PipBoyTypography.labelSmall.copy(fontSize = 10.sp, color = Color.Black),
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }
        }

        // Card 4: System Telemetry (Clean & Minimal)
        SettingsCard(
            title = "ROBCO SYSTEM DIAGNOSTICS",
            borderColor = borderColor,
            icon = Icons.Default.DisplaySettings
        ) {
            SettingTelemetryItem(label = "HARDWARE", value = "PIP-BOY 3000 MK IV")
            SettingTelemetryItem(label = "AUDIO ENGINE", value = "ANDROID MEDIA3 / EXOPLAYER")
            SettingTelemetryItem(label = "ACTIVE STATIONS", value = "$totalStationCount FREQUENCIES")
            SettingTelemetryItem(label = "STATUS", value = "ONLINE // SYNCHRONIZED")
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    borderColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .background(SurfaceCard, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PhosphorPrimary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "[ $title ]",
                style = PipBoyTypography.labelSmall.copy(
                    fontSize = 11.sp,
                    color = PhosphorPrimary
                )
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isOverdrive: Boolean,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = PipBoyTypography.bodyLarge.copy(fontSize = 12.sp, color = PhosphorPrimary)
            )
            Text(
                text = subtitle,
                style = PipBoyTypography.bodyMedium.copy(fontSize = 10.sp, color = PhosphorSecondary.copy(alpha = 0.7f))
            )
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = if (isOverdrive) AccentRed else PhosphorPrimary,
                uncheckedThumbColor = PhosphorSecondary,
                uncheckedTrackColor = Color(0x33000000),
                uncheckedBorderColor = SurfaceBorder.copy(alpha = 0.5f)
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun SettingTelemetryItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "> $label:",
            style = PipBoyTypography.bodyMedium.copy(
                fontSize = 11.sp,
                color = PhosphorSecondary
            )
        )
        Text(
            text = value,
            style = PipBoyTypography.bodyLarge.copy(
                fontSize = 11.sp,
                color = PhosphorPrimary
            )
        )
    }
}
