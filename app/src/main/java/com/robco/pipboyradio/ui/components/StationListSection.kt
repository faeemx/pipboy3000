package com.robco.pipboyradio.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
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
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StationListSection(
    stations: List<Station>,
    currentStation: Station?,
    isPlaying: Boolean,
    searchQuery: String,
    selectedCategory: String,
    isOverdrive: Boolean,
    onSearchChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onStationSelect: (Station) -> Unit,
    onToggleFavorite: (Station) -> Unit,
    onRemoveStation: (Station) -> Unit,
    onRemoveMultipleStations: (Set<String>) -> Unit = {},
    onOpenAddStation: (initialName: String, initialUrl: String) -> Unit,
    // Satellite search integration
    searchResults: List<Station> = emptyList(),
    isSearching: Boolean = false,
    searchError: String? = null,
    onSearchOnline: (query: String, filterType: String) -> Unit = { _, _ -> },
    onClearSearchResults: () -> Unit = {},
    onAddSearchResult: (Station) -> Unit = {},
    onAddAllSearchResults: () -> Unit = {},
    activeStationUrls: Set<String> = emptySet(),
    modifier: Modifier = Modifier
) {
    val categories = listOf("ALL", "FAVORITES", "WASTELAND", "VIVIDH BHARATI", "FM", "AKASHVANI", "JAZZ", "CLASSICAL", "AMBIENT")
    val borderColor = if (isOverdrive) AccentRed else SurfaceBorder

    var isSelectionMode by rememberSaveable { mutableStateOf(false) }
    var selectedUrls by remember { mutableStateOf(setOf<String>()) }
    var isLocalMode by rememberSaveable { mutableStateOf(true) }
    var selectedFilterType by rememberSaveable { mutableStateOf("name") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("station_list_section"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Multi-Selection Action Header (When in Multi-Select Mode)
        AnimatedVisibility(
            visible = isSelectionMode,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (isOverdrive) AccentRed else AccentAmber, RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E160A), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("selection_action_bar")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                isSelectionMode = false
                                selectedUrls = emptySet()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Exit selection",
                                tint = AccentAmber,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = "[ ${selectedUrls.size} SELECTED ]",
                            style = PipBoyTypography.titleMedium.copy(
                                fontSize = 12.sp,
                                color = AccentAmber
                            )
                        )
                    }

                    // Selection Actions: ALL, INVERT, DELETE
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Select All Button
                        OutlinedButton(
                            onClick = {
                                selectedUrls = stations.map { it.url }.toSet()
                            },
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PhosphorPrimary),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(PhosphorPrimary.copy(alpha = 0.5f))
                            ),
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("select_all_btn")
                        ) {
                            Text("ALL", style = PipBoyTypography.labelSmall.copy(fontSize = 10.sp, color = PhosphorPrimary))
                        }

                        // Invert Selection Button
                        OutlinedButton(
                            onClick = {
                                val allUrls = stations.map { it.url }.toSet()
                                selectedUrls = allUrls - selectedUrls
                            },
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PhosphorSecondary),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(PhosphorSecondary.copy(alpha = 0.5f))
                            ),
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("invert_selection_btn")
                        ) {
                            Text("INVERT", style = PipBoyTypography.labelSmall.copy(fontSize = 9.sp, color = PhosphorSecondary))
                        }

                        // Delete Selected Button
                        Button(
                            onClick = {
                                if (selectedUrls.isNotEmpty()) {
                                    onRemoveMultipleStations(selectedUrls)
                                    isSelectionMode = false
                                    selectedUrls = emptySet()
                                }
                            },
                            enabled = selectedUrls.isNotEmpty(),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentRed,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("delete_selected_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "DELETE (${selectedUrls.size})",
                                style = PipBoyTypography.labelSmall.copy(fontSize = 10.sp, color = Color.Black),
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Segmented Mode Switcher: LOCAL TUNER vs SATELLITE SCAN
        if (!isSelectionMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Local Tuner Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            1.dp,
                            if (isLocalMode) (if (isOverdrive) AccentRed else PhosphorPrimary) else SurfaceBorder.copy(alpha = 0.3f),
                            RoundedCornerShape(6.dp)
                        )
                        .background(
                            if (isLocalMode) (if (isOverdrive) AccentRed.copy(alpha = 0.2f) else PhosphorSecondary.copy(alpha = 0.25f)) else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { isLocalMode = true }
                        .padding(vertical = 8.dp)
                        .testTag("tuner_tab_local"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "MY TUNER (${stations.size})",
                        style = PipBoyTypography.labelSmall.copy(
                            fontSize = 11.sp,
                            color = if (isLocalMode) (if (isOverdrive) AccentRed else PhosphorPrimary) else PhosphorSecondary
                        )
                    )
                }
                // Satellite Scan Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            1.dp,
                            if (!isLocalMode) (if (isOverdrive) AccentRed else AccentAmber) else SurfaceBorder.copy(alpha = 0.3f),
                            RoundedCornerShape(6.dp)
                        )
                        .background(
                            if (!isLocalMode) AccentAmber.copy(alpha = 0.15f) else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { isLocalMode = false }
                        .padding(vertical = 8.dp)
                        .testTag("tuner_tab_satellite"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SATELLITE SCAN",
                        style = PipBoyTypography.labelSmall.copy(
                            fontSize = 11.sp,
                            color = if (!isLocalMode) AccentAmber else PhosphorSecondary
                        )
                    )
                }
            }
        }

        if (isLocalMode) {
            // Standard Search & Filter Header (Hidden while multi-selecting to avoid clutter)
            if (!isSelectionMode) {
                // Search bar with integrated Clear button
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text(
                            "SCAN / TYPE NAME OR URL TO ADD...",
                            style = PipBoyTypography.bodyMedium.copy(fontSize = 12.sp)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (isOverdrive) AccentRed else PhosphorPrimary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = PhosphorSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isOverdrive) AccentRed else PhosphorPrimary,
                        unfocusedBorderColor = SurfaceBorder.copy(alpha = 0.5f),
                        focusedTextColor = if (isOverdrive) AccentRed else PhosphorPrimary,
                        unfocusedTextColor = PhosphorSecondary,
                        cursorColor = AccentAmber,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    textStyle = PipBoyTypography.bodyLarge.copy(fontSize = 13.sp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input")
                )

                // Categories Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        val label = if (cat == "FAVORITES") "★ FAVORITES" else cat
                        Box(
                            modifier = Modifier
                                .border(
                                    1.dp,
                                    if (isSelected) (if (isOverdrive) AccentRed else PhosphorPrimary) else SurfaceBorder.copy(alpha = 0.4f),
                                    RoundedCornerShape(6.dp)
                                )
                                .background(
                                    if (isSelected) {
                                        if (isOverdrive) AccentRed.copy(alpha = 0.25f) else PhosphorSecondary.copy(alpha = 0.3f)
                                    } else {
                                        Color.Transparent
                                    },
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { onCategoryChange(cat) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("category_chip_$cat")
                        ) {
                            Text(
                                text = label,
                                style = PipBoyTypography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    color = if (isSelected) {
                                        if (isOverdrive) AccentRed else PhosphorPrimary
                                    } else {
                                        PhosphorSecondary
                                    }
                                )
                            )
                        }
                    }
                }

                // Sub-header row: Frequency tally & Enter Multi-Select shortcut
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "> ${stations.size} FREQUENCIES DETECTED",
                        style = PipBoyTypography.labelSmall.copy(
                            fontSize = 11.sp,
                            color = AccentAmber
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Clean + ADD Button next to SELECT
                        Box(
                            modifier = Modifier
                                .border(1.dp, if (isOverdrive) AccentRed else PhosphorPrimary, RoundedCornerShape(4.dp))
                                .background(if (isOverdrive) AccentRed.copy(alpha = 0.15f) else PhosphorPrimary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .clickable {
                                    val isUrl = searchQuery.startsWith("http://") || searchQuery.startsWith("https://")
                                    onOpenAddStation(
                                        if (isUrl) "" else searchQuery,
                                        if (isUrl) searchQuery else ""
                                    )
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .testTag("tuner_add_station_btn")
                        ) {
                            Text(
                                text = "+ ADD",
                                style = PipBoyTypography.labelSmall.copy(fontSize = 9.sp, color = if (isOverdrive) AccentRed else PhosphorPrimary)
                            )
                        }

                        // Quick Multi-Select Mode Trigger Button
                        Box(
                            modifier = Modifier
                                .border(1.dp, SurfaceBorder.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .background(Color(0x22000000), RoundedCornerShape(4.dp))
                                .clickable {
                                    isSelectionMode = true
                                    selectedUrls = emptySet()
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .testTag("enter_select_mode_btn")
                        ) {
                            Text(
                                text = "SELECT",
                                style = PipBoyTypography.labelSmall.copy(fontSize = 9.sp, color = PhosphorSecondary)
                            )
                        }

                        if (selectedCategory != "ALL") {
                            Text(
                                text = "[ $selectedCategory ]",
                                style = PipBoyTypography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = PhosphorSecondary
                                )
                            )
                        }
                    }
                }
            }



        // Station List LazyColumn
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("stations_lazy_column"),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (stations.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "SIGNAL LOST // NO BROADCAST DETECTED",
                                style = PipBoyTypography.titleMedium.copy(
                                    color = PhosphorSecondary.copy(alpha = 0.7f),
                                    fontSize = 13.sp
                                )
                            )

                            if (searchQuery.isNotBlank()) {
                                Text(
                                    text = "NO LOCAL STATIONS MATCH \"$searchQuery\"",
                                    style = PipBoyTypography.bodyMedium.copy(
                                        fontSize = 11.sp,
                                        color = AccentAmber
                                    )
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            val isUrl = searchQuery.startsWith("http://") || searchQuery.startsWith("https://")
                                            onOpenAddStation(
                                                if (isUrl) "" else searchQuery,
                                                if (isUrl) searchQuery else ""
                                            )
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isOverdrive) AccentRed else PhosphorPrimary,
                                            contentColor = Color.Black
                                        ),
                                        modifier = Modifier.testTag("empty_state_add_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "+ ADD AS STATION",
                                            style = PipBoyTypography.labelSmall.copy(fontSize = 10.sp, color = Color.Black),
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            isLocalMode = false
                                            if (searchQuery.isNotBlank()) {
                                                onSearchOnline(searchQuery, selectedFilterType)
                                            }
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentAmber),
                                        border = ButtonDefaults.outlinedButtonBorder.copy(
                                            brush = androidx.compose.ui.graphics.SolidColor(AccentAmber.copy(alpha = 0.6f))
                                        ),
                                        modifier = Modifier.testTag("empty_state_satellite_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Public,
                                            contentDescription = null,
                                            tint = AccentAmber,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "SCAN SATELLITES",
                                            style = PipBoyTypography.labelSmall.copy(fontSize = 10.sp, color = AccentAmber),
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(stations, key = { index, s -> "${s.url}_$index" }) { index, station ->
                    val isActive = station.url == currentStation?.url
                    val isStationPlaying = isActive && isPlaying
                    val isSelected = selectedUrls.contains(station.url)

                    TelegramSwipeableStationItem(
                        station = station,
                        index = index,
                        isActive = isActive,
                        isStationPlaying = isStationPlaying,
                        isOverdrive = isOverdrive,
                        isSelectionMode = isSelectionMode,
                        isSelected = isSelected,
                        onStationSelect = {
                            if (isSelectionMode) {
                                selectedUrls = if (isSelected) selectedUrls - station.url else selectedUrls + station.url
                            } else {
                                onStationSelect(station)
                            }
                        },
                        onStationLongPress = {
                            if (!isSelectionMode) {
                                isSelectionMode = true
                                selectedUrls = setOf(station.url)
                            } else {
                                selectedUrls = if (isSelected) selectedUrls - station.url else selectedUrls + station.url
                            }
                        },
                        onToggleFavorite = { onToggleFavorite(station) },
                        onRemoveStation = { onRemoveStation(station) }
                    )
                }
            }
        }
    } else {
            // SATELLITE SCAN MODE - Online Radio-Browser Directory
            // Filter Type Selector Pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                listOf(
                    "name" to "NAME",
                    "tag" to "GENRE",
                    "country" to "COUNTRY",
                    "language" to "LANGUAGE"
                ).forEach { (type, label) ->
                    val isSel = selectedFilterType == type
                    Box(
                        modifier = Modifier
                            .border(
                                1.dp,
                                if (isSel) AccentAmber else SurfaceBorder.copy(alpha = 0.4f),
                                RoundedCornerShape(4.dp)
                            )
                            .background(
                                if (isSel) AccentAmber.copy(alpha = 0.2f) else Color.Transparent,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable { selectedFilterType = type }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            style = PipBoyTypography.labelSmall.copy(
                                fontSize = 11.sp,
                                color = if (isSel) AccentAmber else PhosphorSecondary
                            )
                        )
                    }
                }
            }

            // Satellite Search Bar
            var satelliteQuery by rememberSaveable { mutableStateOf("") }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = satelliteQuery,
                    onValueChange = { satelliteQuery = it },
                    singleLine = true,
                    placeholder = {
                        Text(
                            "SCAN: jazz, bbc, india, ambient...",
                            style = PipBoyTypography.bodyMedium.copy(fontSize = 12.sp)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Satellite",
                            tint = AccentAmber
                        )
                    },
                    trailingIcon = {
                        if (satelliteQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                satelliteQuery = ""
                                onClearSearchResults()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = PhosphorSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    textStyle = PipBoyTypography.bodyLarge.copy(fontSize = 13.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentAmber,
                        unfocusedBorderColor = SurfaceBorder.copy(alpha = 0.5f),
                        focusedTextColor = AccentAmber,
                        unfocusedTextColor = PhosphorSecondary,
                        cursorColor = AccentAmber,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("satellite_search_input")
                )

                Button(
                    onClick = { onSearchOnline(satelliteQuery, selectedFilterType) },
                    enabled = !isSearching && satelliteQuery.isNotBlank(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentAmber,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .height(50.dp)
                        .testTag("satellite_search_btn")
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.Black
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (searchError != null) {
                Text(
                    text = "> $searchError",
                    style = PipBoyTypography.labelSmall.copy(fontSize = 10.sp, color = AccentRed),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            // Search Results Header with ADD ALL
            if (searchResults.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "> ${searchResults.size} SATELLITE SIGNALS",
                        style = PipBoyTypography.labelSmall.copy(fontSize = 11.sp, color = AccentAmber)
                    )

                    Button(
                        onClick = onAddAllSearchResults,
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PhosphorPrimary,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("satellite_add_all_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "ADD ALL",
                            style = PipBoyTypography.labelSmall.copy(fontSize = 9.sp, color = Color.Black),
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }

            // Search Results List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("satellite_results_list"),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (searchResults.isEmpty() && !isSearching && satelliteQuery.isNotBlank()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "NO SATELLITE SIGNALS DETECTED",
                                style = PipBoyTypography.titleMedium.copy(
                                    color = PhosphorSecondary.copy(alpha = 0.7f),
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                } else {
                    itemsIndexed(searchResults, key = { index, s -> "sat_${s.url}_$index" }) { index, station ->
                        val isAlreadyAdded = activeStationUrls.contains(station.url)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x22000000), RoundedCornerShape(4.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = station.name,
                                    style = PipBoyTypography.bodyLarge.copy(
                                        fontSize = 12.sp,
                                        color = if (isAlreadyAdded) PhosphorSecondary else PhosphorPrimary
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "[ ${station.category.uppercase()} ]",
                                    style = PipBoyTypography.bodyMedium.copy(
                                        fontSize = 9.sp,
                                        color = PhosphorSecondary.copy(alpha = 0.7f)
                                    )
                                )
                            }

                            if (isAlreadyAdded) {
                                Box(
                                    modifier = Modifier
                                        .background(PhosphorSecondary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = PhosphorPrimary,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Text(
                                            text = "ADDED",
                                            style = PipBoyTypography.labelSmall.copy(fontSize = 9.sp, color = PhosphorPrimary)
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { onAddSearchResult(station) },
                                    shape = RoundedCornerShape(4.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isOverdrive) AccentRed else PhosphorPrimary,
                                        contentColor = Color.Black
                                    ),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add",
                                        tint = Color.Black,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "ADD",
                                        style = PipBoyTypography.labelSmall.copy(fontSize = 9.sp, color = Color.Black),
                                        modifier = Modifier.padding(start = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Telegram-Style Swipeable Row:
 * - Swiping from right to left reveals a red Telegram-like "DELETE" button.
 * - Tapping the revealed delete button deletes the station.
 * - Swiping past threshold (> 140dp) auto-deletes the station.
 * - Press & hold (long press) enters Multi-Select Mode to select more, select all, etc.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TelegramSwipeableStationItem(
    station: Station,
    index: Int,
    isActive: Boolean,
    isStationPlaying: Boolean,
    isOverdrive: Boolean,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onStationSelect: () -> Unit,
    onStationLongPress: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRemoveStation: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val revealWidth = 84.dp
    val revealWidthPx = with(density) { revealWidth.toPx() }
    val fullSwipeThresholdPx = with(density) { 150.dp.toPx() }

    val offsetX = remember { Animatable(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF0F1511))
            .testTag("station_item_container_$index")
    ) {
        // Background revealed Telegram-style DELETE button on the right
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(AccentRed, RoundedCornerShape(6.dp))
                .clickable {
                    coroutineScope.launch {
                        offsetX.animateTo(0f, tween(100))
                        onRemoveStation()
                    }
                }
                .padding(end = 16.dp)
                .testTag("swipe_delete_action_$index"),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "DELETE",
                    style = PipBoyTypography.labelSmall.copy(
                        fontSize = 11.sp,
                        color = Color.Black
                    )
                )
            }
        }

        // Foreground Station Card (Draggable horizontally to reveal delete button)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(isSelectionMode) {
                    if (!isSelectionMode) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, dragAmount ->
                                val target = (offsetX.value + dragAmount).coerceIn(-fullSwipeThresholdPx * 1.3f, 0f)
                                coroutineScope.launch { offsetX.snapTo(target) }
                            },
                            onDragEnd = {
                                coroutineScope.launch {
                                    if (offsetX.value < -fullSwipeThresholdPx) {
                                        // Full swipe to dismiss and delete!
                                        offsetX.animateTo(-fullSwipeThresholdPx * 2, tween(150))
                                        onRemoveStation()
                                    } else if (offsetX.value < -revealWidthPx * 0.4f) {
                                        // Snap open to reveal Telegram delete option
                                        offsetX.animateTo(-revealWidthPx, tween(150))
                                    } else {
                                        // Snap closed
                                        offsetX.animateTo(0f, tween(150))
                                    }
                                }
                            },
                            onDragCancel = {
                                coroutineScope.launch { offsetX.animateTo(0f, tween(150)) }
                            }
                        )
                    }
                }
                .border(
                    width = if (isSelected) 1.5.dp else if (isActive) 1.dp else 0.5.dp,
                    color = when {
                        isSelected -> AccentAmber
                        isActive -> if (isOverdrive) AccentRed else PhosphorPrimary
                        else -> SurfaceBorder.copy(alpha = 0.3f)
                    },
                    shape = RoundedCornerShape(6.dp)
                )
                .background(
                    when {
                        isSelected -> AccentAmber.copy(alpha = 0.2f)
                        isActive -> if (isOverdrive) AccentRed.copy(alpha = 0.15f) else PhosphorSecondary.copy(alpha = 0.15f)
                        else -> SurfaceCard
                    },
                    RoundedCornerShape(6.dp)
                )
                .combinedClickable(
                    onClick = {
                        if (offsetX.value < -5f) {
                            coroutineScope.launch { offsetX.animateTo(0f, tween(150)) }
                        } else {
                            onStationSelect()
                        }
                    },
                    onLongClick = onStationLongPress
                )
                .padding(horizontal = 10.dp, vertical = 7.dp)
                .testTag("station_item_$index"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Multi-select Checkbox indicator
                if (isSelectionMode) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (isSelected) "Selected" else "Not Selected",
                        tint = if (isSelected) AccentAmber else PhosphorSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Station Info Column
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isActive && !isSelectionMode) {
                            Text(
                                text = if (isStationPlaying) "►" else "■",
                                style = PipBoyTypography.bodyLarge.copy(
                                    color = if (isOverdrive) AccentRed else AccentAmber,
                                    fontSize = 12.sp
                                )
                            )
                        }

                        Text(
                            text = station.name,
                            style = PipBoyTypography.bodyLarge.copy(
                                color = when {
                                    isSelected -> AccentAmber
                                    isActive -> if (isOverdrive) AccentRed else PhosphorPrimary
                                    else -> Color(0xFFDDFFDD)
                                },
                                fontSize = 13.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "[ ${station.category.uppercase()} ]",
                        style = PipBoyTypography.bodyMedium.copy(
                            fontSize = 10.sp,
                            color = PhosphorSecondary.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Right Action Buttons (Favorite Star & Quick Delete)
            if (!isSelectionMode) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("favorite_btn_$index")
                    ) {
                        Icon(
                            imageVector = if (station.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = if (station.isFavorite) "Favorited" else "Favorite",
                            tint = if (station.isFavorite) AccentAmber else PhosphorSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete button (can also be swiped to reveal!)
                    IconButton(
                        onClick = onRemoveStation,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("delete_station_btn_$index")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete station",
                            tint = AccentRed.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
