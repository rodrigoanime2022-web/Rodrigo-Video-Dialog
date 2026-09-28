package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CharacterConfig
import com.example.data.model.SampleScenario
import com.example.ui.components.CharacterConfigCard
import com.example.ui.components.HeroHeader
import com.example.ui.components.ProcessingOverlay
import com.example.ui.components.VideoInputSection
import com.example.ui.theme.RodrigoAmber
import com.example.util.VideoUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    videoUrl: String,
    onVideoUrlChange: (String) -> Unit,
    urlError: String?,
    uploadedVideoMeta: VideoUtils.VideoMeta?,
    uploadedVideoUri: Uri?,
    onVideoSelected: (Uri?) -> Unit,
    onClearUploadedVideo: () -> Unit,
    sampleScenarios: List<SampleScenario>,
    selectedSample: SampleScenario?,
    onSelectSample: (SampleScenario) -> Unit,
    characterConfig: CharacterConfig,
    onUpdateCharacterConfig: (name: String, keyword: String, entrancePhrase: String, tone: String) -> Unit,
    isAnalyzing: Boolean,
    analysisStageText: String,
    analysisProgress: Float,
    savedScriptsCount: Int,
    onOpenSavedScripts: () -> Unit,
    onTransformClick: () -> Unit,
    toastMessage: String?,
    onClearToast: () -> Unit,
    customApiKey: String,
    onSaveCustomKey: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showSettingsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(toastMessage) {
        if (!toastMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(toastMessage)
            onClearToast()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Rodrigo Video Dialog",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = "Logo",
                            tint = RodrigoAmber
                        )
                    }
                },
                actions = {
                    // Saved Scripts button with badge
                    IconButton(
                        onClick = onOpenSavedScripts,
                        modifier = Modifier.testTag("open_saved_scripts_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (savedScriptsCount > 0) {
                                    Badge(containerColor = RodrigoAmber) {
                                        Text("$savedScriptsCount", color = androidx.compose.ui.graphics.Color.Black)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmarks,
                                contentDescription = "Roteiros Salvos"
                            )
                        }
                    }

                    // Settings button
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configurações"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 32.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Hero Header
                item {
                    HeroHeader()
                }

                // Character Config (Rodrigo)
                item {
                    CharacterConfigCard(
                        config = characterConfig,
                        onConfigChange = onUpdateCharacterConfig
                    )
                }

                // Video input section (Link / Upload / Samples) + Big "Transformar em Diálogo" Button
                item {
                    VideoInputSection(
                        selectedTab = selectedTab,
                        onTabSelected = onTabSelected,
                        videoUrl = videoUrl,
                        onVideoUrlChange = onVideoUrlChange,
                        urlError = urlError,
                        uploadedVideoMeta = uploadedVideoMeta,
                        uploadedVideoUri = uploadedVideoUri,
                        onVideoSelected = onVideoSelected,
                        onClearUploadedVideo = onClearUploadedVideo,
                        sampleScenarios = sampleScenarios,
                        selectedSample = selectedSample,
                        onSelectSample = onSelectSample,
                        onTransformClick = onTransformClick
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Processing overlay dialog during AI generation
            if (isAnalyzing) {
                ProcessingOverlay(
                    stageText = analysisStageText,
                    progress = analysisProgress,
                    characterName = characterConfig.name
                )
            }

            // Settings dialog
            if (showSettingsDialog) {
                SettingsDialog(
                    currentCustomKey = customApiKey,
                    onSaveCustomKey = onSaveCustomKey,
                    onDismiss = { showSettingsDialog = false }
                )
            }
        }
    }
}
