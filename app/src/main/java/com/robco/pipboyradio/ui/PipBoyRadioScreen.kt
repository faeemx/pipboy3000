package com.robco.pipboyradio.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.robco.pipboyradio.data.model.PipBoyTab
import com.robco.pipboyradio.ui.components.AddStationDialog
import com.robco.pipboyradio.ui.components.MetadataDialog
import com.robco.pipboyradio.ui.components.MiniPlayerBar
import com.robco.pipboyradio.ui.components.PipBoyBottomNav
import com.robco.pipboyradio.ui.components.ReceiverScreen
import com.robco.pipboyradio.ui.components.StationListSection
import com.robco.pipboyradio.ui.components.VaultLogsSection
import com.robco.pipboyradio.ui.theme.ScreenBackground
import com.robco.pipboyradio.ui.viewmodel.RadioViewModel
import kotlin.math.roundToInt

@Composable
fun PipBoyRadioScreen(
    viewModel: RadioViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredStations = viewModel.getFilteredStations()

    // Handle back button on sub-screens to return to Receiver
    if (uiState.currentTab != PipBoyTab.RECEIVER) {
        BackHandler {
            viewModel.setTab(PipBoyTab.RECEIVER)
        }
    }

    // Shake animation on overdrive
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(uiState.isOverdrive) {
        if (uiState.isOverdrive) {
            repeat(10) {
                shakeOffset.animateTo(4f, tween(50))
                shakeOffset.animateTo(-4f, tween(50))
            }
            shakeOffset.animateTo(0f, tween(50))
        }
    }

    // Modal Add Station Dialog
    AddStationDialog(
        isOpen = uiState.showAddStationDialog,
        initialName = uiState.initialAddStationName,
        initialUrl = uiState.initialAddStationUrl,
        isOverdrive = uiState.isOverdrive,
        onDismiss = { viewModel.closeAddStationDialog() },
        onAddStation = { viewModel.addStationToTuner(it) }
    )

    // Modal Live Metadata Dialog
    MetadataDialog(
        isOpen = uiState.showMetadataDialog,
        station = uiState.currentStation,
        metadata = uiState.streamMetadata,
        isPlaying = uiState.isPlaying,
        isOverdrive = uiState.isOverdrive,
        onDismiss = { viewModel.closeMetadataDialog() }
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("pipboy_scaffold"),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = ScreenBackground,
        bottomBar = {
            PipBoyBottomNav(
                currentTab = uiState.currentTab,
                isOverdrive = uiState.isOverdrive,
                onTabSelected = { viewModel.setTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Main Tab Content
                Box(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = uiState.currentTab,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(150)) togetherWith fadeOut(animationSpec = tween(150))
                        },
                        label = "TabContentTransition"
                    ) { tab ->
                        when (tab) {
                            PipBoyTab.RECEIVER -> {
                                ReceiverScreen(
                                    currentStation = uiState.currentStation,
                                    isPlaying = uiState.isPlaying,
                                    statusSignal = uiState.displayStatus,
                                    streamMetadata = uiState.streamMetadata,
                                    nowPlayingText = uiState.nowPlayingDisplay,
                                    volume = uiState.volume,
                                    isOverdrive = uiState.isOverdrive,
                                    onTogglePower = { viewModel.togglePower() },
                                    onPrevStation = { viewModel.prevStation() },
                                    onNextStation = { viewModel.nextStation() },
                                    onVolumeChange = { viewModel.setVolume(it) },
                                    onToggleOverdrive = { viewModel.toggleOverdrive() },
                                    onOpenMetadata = { viewModel.openMetadataDialog() }
                                )
                            }

                            PipBoyTab.TUNER -> {
                                StationListSection(
                                    stations = filteredStations,
                                    currentStation = uiState.currentStation,
                                    isPlaying = uiState.isPlaying,
                                    searchQuery = uiState.searchQuery,
                                    selectedCategory = uiState.selectedCategory,
                                    isOverdrive = uiState.isOverdrive,
                                    onSearchChange = { viewModel.setSearchQuery(it) },
                                    onCategoryChange = { viewModel.setCategory(it) },
                                    onStationSelect = { viewModel.playStation(it) },
                                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                                    onRemoveStation = { viewModel.removeStationFromTuner(it) },
                                    onRemoveMultipleStations = { viewModel.removeStationsFromTuner(it) },
                                    onOpenAddStation = { name, url -> viewModel.openAddStationDialog(name, url) },
                                    searchResults = uiState.searchResults,
                                    isSearching = uiState.isSearching,
                                    searchError = uiState.searchError,
                                    onSearchOnline = { query, filterType -> viewModel.searchOnlineStations(query, filterType) },
                                    onClearSearchResults = { viewModel.clearSearchResults() },
                                    onAddSearchResult = { viewModel.addStationToTuner(it) },
                                    onAddAllSearchResults = { viewModel.addAllSearchResultsToTuner() },
                                    activeStationUrls = filteredStations.map { it.url }.toSet()
                                )
                            }

                            PipBoyTab.SETTINGS -> {
                                VaultLogsSection(
                                    isOverdrive = uiState.isOverdrive,
                                    isRepoLoading = uiState.isRepoLoading,
                                    repoStatusMessage = uiState.repoStatusMessage,
                                    totalStationCount = uiState.totalStationCount,
                                    showRepoModal = uiState.showRepoModal,
                                    autoPlayNextOnError = uiState.autoPlayNextOnError,
                                    dataSaverBuffer = uiState.dataSaverBuffer,
                                    scanlinesEnabled = uiState.scanlinesEnabled,
                                    pipBoyColorTheme = uiState.pipBoyColorTheme,
                                    visualizerStyleName = uiState.visualizerStyleName,
                                    onToggleOverdrive = { viewModel.toggleOverdrive() },
                                    onToggleAutoPlayNext = { viewModel.toggleAutoPlayNextOnError() },
                                    onToggleDataSaver = { viewModel.toggleDataSaver() },
                                    onToggleScanlines = { viewModel.toggleScanlines() },
                                    onSelectColorTheme = { viewModel.setColorTheme(it) },
                                    onSelectVisualizerStyle = { viewModel.setVisualizerStyle(it) },
                                    onOpenAddStationDialog = { viewModel.openAddStationDialog() },
                                    onOpenRepoModal = { viewModel.setShowRepoModal(true) },
                                    onCloseRepoModal = { viewModel.setShowRepoModal(false) },
                                    onRemoveStation = { viewModel.removeStationFromTuner(it) },
                                    onLoadRawJsonUrl = { url, append -> viewModel.loadFromRawJsonUrl(url, append) },
                                    onLoadBundledStations = { append -> viewModel.loadBundledStations(append) },
                                    onRestoreDefaults = { viewModel.restoreDefaultStations() },
                                    onClearAllStations = { viewModel.clearAllStations() }
                                )
                            }
                        }
                    }
                }

                // Mini Player docked above bottom nav when browsing TUNER or SETTINGS
                if (uiState.currentTab != PipBoyTab.RECEIVER) {
                    MiniPlayerBar(
                        station = uiState.currentStation,
                        isPlaying = uiState.isPlaying,
                        statusSignal = uiState.displayStatus,
                        isOverdrive = uiState.isOverdrive,
                        onTogglePower = { viewModel.togglePower() },
                        onNextStation = { viewModel.nextStation() },
                        onExpand = { viewModel.setTab(PipBoyTab.RECEIVER) }
                    )
                }
            }

            // CRT Scanline Overlay
            if (uiState.scanlinesEnabled) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val lineSpacing = 4.dp.toPx()
                    var y = 0f
                    val scanlineColor = Color(0x0C000000)
                    while (y < size.height) {
                        drawLine(
                            color = scanlineColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                        y += lineSpacing
                    }
                }
            }
        }
    }
}
