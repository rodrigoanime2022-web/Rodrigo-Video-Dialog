package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SavedScriptsScreen
import com.example.ui.screens.ScriptResultScreen
import com.example.ui.theme.RodrigoVideoDialogTheme
import com.example.ui.viewmodel.VideoDialogViewModel

enum class AppScreen {
    HOME,
    SAVED_SCRIPTS
}

class MainActivity : ComponentActivity() {

    private val viewModel: VideoDialogViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RodrigoVideoDialogTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RodrigoVideoDialogApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun RodrigoVideoDialogApp(viewModel: VideoDialogViewModel) {
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val videoUrl by viewModel.videoUrl.collectAsStateWithLifecycle()
    val urlError by viewModel.urlError.collectAsStateWithLifecycle()
    val uploadedVideoMeta by viewModel.uploadedVideoMeta.collectAsStateWithLifecycle()
    val uploadedVideoUri by viewModel.uploadedVideoUri.collectAsStateWithLifecycle()
    val selectedSample by viewModel.selectedSample.collectAsStateWithLifecycle()
    val characterConfig by viewModel.characterConfig.collectAsStateWithLifecycle()

    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val analysisStageText by viewModel.analysisStageText.collectAsStateWithLifecycle()
    val analysisProgress by viewModel.analysisProgress.collectAsStateWithLifecycle()

    val currentScript by viewModel.currentScript.collectAsStateWithLifecycle()
    val isCurrentSaved by viewModel.isCurrentSaved.collectAsStateWithLifecycle()

    val savedScripts by viewModel.savedScripts.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val customApiKey by viewModel.customApiKey.collectAsStateWithLifecycle()

    // If a script is currently generated or opened, display the Script Result & Editor screen
    if (currentScript != null) {
        ScriptResultScreen(
            script = currentScript!!,
            isSaved = isCurrentSaved,
            onBack = { viewModel.dismissCurrentScript() },
            onSave = { viewModel.saveCurrentScript() },
            onCopy = { ctx -> viewModel.copyScriptToClipboard(ctx) },
            onExport = { ctx -> viewModel.exportAndShareScript(ctx) },
            onUpdateTitle = { newTitle -> viewModel.updateScriptTitle(newTitle) },
            onUpdateLine = { lineId, newSpeaker, newText, newAction, isRodrigo ->
                viewModel.updateLine(lineId, newSpeaker, newText, newAction, isRodrigo)
            },
            onDeleteLine = { lineId -> viewModel.deleteLine(lineId) },
            onAddLine = { afterIndex, isRodrigo -> viewModel.addLine(afterIndex, isRodrigo) }
        )
    } else {
        when (currentScreen) {
            AppScreen.HOME -> {
                HomeScreen(
                    selectedTab = selectedTab,
                    onTabSelected = { viewModel.setSelectedTab(it) },
                    videoUrl = videoUrl,
                    onVideoUrlChange = { viewModel.setVideoUrl(it) },
                    urlError = urlError,
                    uploadedVideoMeta = uploadedVideoMeta,
                    uploadedVideoUri = uploadedVideoUri,
                    onVideoSelected = { viewModel.handleVideoUri(it) },
                    onClearUploadedVideo = { viewModel.clearUploadedVideo() },
                    sampleScenarios = viewModel.sampleScenarios,
                    selectedSample = selectedSample,
                    onSelectSample = { viewModel.selectSample(it) },
                    characterConfig = characterConfig,
                    onUpdateCharacterConfig = { name, keyword, entrancePhrase, tone ->
                        viewModel.updateCharacterConfig(name, keyword, entrancePhrase, tone)
                    },
                    isAnalyzing = isAnalyzing,
                    analysisStageText = analysisStageText,
                    analysisProgress = analysisProgress,
                    savedScriptsCount = savedScripts.size,
                    onOpenSavedScripts = { currentScreen = AppScreen.SAVED_SCRIPTS },
                    onTransformClick = { viewModel.startAnalysis() },
                    toastMessage = toastMessage,
                    onClearToast = { viewModel.clearToast() },
                    customApiKey = customApiKey,
                    onSaveCustomKey = { viewModel.setCustomApiKey(it) }
                )
            }
            AppScreen.SAVED_SCRIPTS -> {
                SavedScriptsScreen(
                    savedScripts = savedScripts,
                    onSelectScript = { script ->
                        viewModel.openSavedScript(script)
                        currentScreen = AppScreen.HOME
                    },
                    onDeleteScript = { script -> viewModel.deleteSavedScript(script) },
                    onBack = { currentScreen = AppScreen.HOME }
                )
            }
        }
    }
}
