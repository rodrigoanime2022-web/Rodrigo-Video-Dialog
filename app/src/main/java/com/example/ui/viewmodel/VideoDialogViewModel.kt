package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.CharacterConfig
import com.example.data.model.DialogueLine
import com.example.data.model.SampleScenario
import com.example.data.model.ScriptResult
import com.example.data.remote.VideoAnalysisEngine
import com.example.data.repository.ScriptRepository
import com.example.util.VideoUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class VideoDialogViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ScriptRepository
    private val analysisEngine = VideoAnalysisEngine()

    init {
        val db = AppDatabase.getInstance(application)
        repository = ScriptRepository(db.scriptDao())
    }

    val savedScripts: StateFlow<List<ScriptResult>> = repository.allScripts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Input state
    private val _selectedTab = MutableStateFlow(0) // 0: Link, 1: Upload, 2: Exemplos
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _videoUrl = MutableStateFlow("")
    val videoUrl: StateFlow<String> = _videoUrl.asStateFlow()

    private val _urlError = MutableStateFlow<String?>(null)
    val urlError: StateFlow<String?> = _urlError.asStateFlow()

    private val _uploadedVideoMeta = MutableStateFlow<VideoUtils.VideoMeta?>(null)
    val uploadedVideoMeta: StateFlow<VideoUtils.VideoMeta?> = _uploadedVideoMeta.asStateFlow()

    private val _uploadedVideoUri = MutableStateFlow<Uri?>(null)
    val uploadedVideoUri: StateFlow<Uri?> = _uploadedVideoUri.asStateFlow()

    private val _selectedSample = MutableStateFlow<SampleScenario?>(null)
    val selectedSample: StateFlow<SampleScenario?> = _selectedSample.asStateFlow()

    // Character Config state
    private val _characterConfig = MutableStateFlow(CharacterConfig())
    val characterConfig: StateFlow<CharacterConfig> = _characterConfig.asStateFlow()

    // Analysis state
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisStageText = MutableStateFlow("")
    val analysisStageText: StateFlow<String> = _analysisStageText.asStateFlow()

    private val _analysisProgress = MutableStateFlow(0f)
    val analysisProgress: StateFlow<Float> = _analysisProgress.asStateFlow()

    // Current generated/viewed script
    private val _currentScript = MutableStateFlow<ScriptResult?>(null)
    val currentScript: StateFlow<ScriptResult?> = _currentScript.asStateFlow()

    private val _isCurrentSaved = MutableStateFlow(false)
    val isCurrentSaved: StateFlow<Boolean> = _isCurrentSaved.asStateFlow()

    // Messages and settings
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    val sampleScenarios = listOf(
        SampleScenario(
            id = "sample_1",
            title = "Podcast no Estúdio",
            category = "Entrevista",
            description = "Gravação de podcast sobre novidades e cultura pop onde a bancada discute com seriedade.",
            duration = "01:25",
            defaultLink = "https://www.youtube.com/watch?v=sample_podcast_scene"
        ),
        SampleScenario(
            id = "sample_2",
            title = "Mistério no Prédio",
            category = "Suspense",
            description = "Dois investigadores procurando pistas de uma porta misteriosamente destrancada à noite.",
            duration = "01:15",
            defaultLink = "https://www.youtube.com/watch?v=sample_mystery_office"
        ),
        SampleScenario(
            id = "sample_3",
            title = "Cafeteria da Esquina",
            category = "Cotidiano / Amigos",
            description = "Dois amigos trocando conselhos amorosos e dilemas de carreira na mesa de café.",
            duration = "01:30",
            defaultLink = "https://www.youtube.com/watch?v=sample_coffee_friends"
        )
    )

    fun setSelectedTab(index: Int) {
        _selectedTab.value = index
    }

    fun setVideoUrl(url: String) {
        _videoUrl.value = url
        if (_urlError.value != null) {
            _urlError.value = null
        }
    }

    fun handleVideoUri(uri: Uri?) {
        if (uri == null) return
        _uploadedVideoUri.value = uri
        val meta = VideoUtils.extractMetadata(getApplication(), uri)
        _uploadedVideoMeta.value = meta
        _toastMessage.value = "Vídeo carregado: ${meta.fileName}"
    }

    fun clearUploadedVideo() {
        _uploadedVideoUri.value = null
        _uploadedVideoMeta.value = null
    }

    fun selectSample(sample: SampleScenario) {
        _selectedSample.value = sample
        _videoUrl.value = sample.defaultLink
    }

    fun updateCharacterConfig(
        name: String = _characterConfig.value.name,
        keyword: String = _characterConfig.value.keyword,
        entrancePhrase: String = _characterConfig.value.entrancePhrase,
        tone: String = _characterConfig.value.tone
    ) {
        _characterConfig.value = _characterConfig.value.copy(
            name = name,
            keyword = keyword,
            entrancePhrase = entrancePhrase,
            tone = tone
        )
    }

    fun setCustomApiKey(key: String) {
        _customApiKey.value = key.trim()
    }

    fun startAnalysis() {
        val tab = _selectedTab.value
        var videoDesc = ""
        var videoSourceDesc = ""
        var thumbBase64: String? = null

        when (tab) {
            0 -> {
                // Link
                val url = _videoUrl.value.trim()
                if (url.isBlank()) {
                    _urlError.value = "Por favor, cole o link do vídeo para analisar."
                    return
                }
                if (!VideoUtils.isValidUrl(url)) {
                    _urlError.value = "Insira uma URL válida (ex: https://...)"
                    return
                }
                _urlError.value = null
                videoSourceDesc = "Link: $url"
                videoDesc = "Vídeo compartilhado via link web ($url). Análise de cena, falas e movimentação com entrada surpresa."
            }
            1 -> {
                // Upload
                val meta = _uploadedVideoMeta.value
                if (meta == null) {
                    _toastMessage.value = "Selecione um arquivo de vídeo do seu celular primeiro."
                    return
                }
                videoSourceDesc = "Arquivo local: ${meta.fileName} (${VideoUtils.formatDuration(meta.durationMs)})"
                videoDesc = "Arquivo de vídeo '${meta.fileName}' com duração de ${VideoUtils.formatDuration(meta.durationMs)} e tamanho de ${VideoUtils.formatFileSize(meta.sizeBytes)}."
                thumbBase64 = meta.thumbnailBase64
            }
            2 -> {
                // Sample
                val sample = _selectedSample.value ?: sampleScenarios.first()
                videoSourceDesc = "Cenário Demo: ${sample.title} (${sample.category})"
                videoDesc = "${sample.description} Categoria: ${sample.category}."
            }
        }

        viewModelScope.launch {
            _isAnalyzing.value = true
            _analysisProgress.value = 0.1f
            _analysisStageText.value = "1/4: Analisando frames e trilha sonora..."
            delay(600)

            _analysisProgress.value = 0.35f
            _analysisStageText.value = "2/4: Identificando personagens, ambiente e falas..."
            delay(700)

            _analysisProgress.value = 0.65f
            _analysisStageText.value = "3/4: Orquestrando a entrada de ${_characterConfig.value.name} na cena..."
            delay(600)

            _analysisProgress.value = 0.85f
            _analysisStageText.value = "4/4: Formatando roteiro e diálogo cinematográfico..."

            val script = analysisEngine.analyzeAndGenerateScript(
                videoDesc = videoDesc,
                videoSource = videoSourceDesc,
                thumbnailBase64 = thumbBase64,
                charConfig = _characterConfig.value,
                userCustomApiKey = _customApiKey.value
            )

            _analysisProgress.value = 1.0f
            delay(400)
            _isAnalyzing.value = false
            _currentScript.value = script
            _isCurrentSaved.value = false
            _toastMessage.value = "Roteiro transformado com sucesso!"
        }
    }

    fun openSavedScript(script: ScriptResult) {
        _currentScript.value = script
        _isCurrentSaved.value = true
    }

    fun updateScriptTitle(newTitle: String) {
        val curr = _currentScript.value ?: return
        _currentScript.value = curr.copy(title = newTitle)
        _isCurrentSaved.value = false
    }

    fun updateLine(lineId: String, newSpeaker: String, newText: String, newAction: String, isRodrigo: Boolean) {
        val curr = _currentScript.value ?: return
        val updatedLines = curr.lines.map { line ->
            if (line.id == lineId) {
                line.copy(
                    speaker = newSpeaker,
                    text = newText,
                    action = newAction,
                    isRodrigo = isRodrigo || newSpeaker.equals(_characterConfig.value.name, ignoreCase = true)
                )
            } else {
                line
            }
        }
        _currentScript.value = curr.copy(lines = updatedLines)
        _isCurrentSaved.value = false
    }

    fun deleteLine(lineId: String) {
        val curr = _currentScript.value ?: return
        val updatedLines = curr.lines.filterNot { it.id == lineId }
        _currentScript.value = curr.copy(lines = updatedLines)
        _isCurrentSaved.value = false
        _toastMessage.value = "Linha removida do diálogo"
    }

    fun addLine(afterIndex: Int, isRodrigo: Boolean = false) {
        val curr = _currentScript.value ?: return
        val newLine = DialogueLine(
            id = UUID.randomUUID().toString(),
            speaker = if (isRodrigo) _characterConfig.value.name else "Personagem",
            text = if (isRodrigo) "Eu tenho algo a acrescentar aqui!" else "O que está acontecendo?",
            action = if (isRodrigo) "olhando confiante" else "olhando surpreso",
            isRodrigo = isRodrigo
        )
        val mutable = curr.lines.toMutableList()
        val insertIndex = (afterIndex + 1).coerceIn(0, mutable.size)
        mutable.add(insertIndex, newLine)
        _currentScript.value = curr.copy(lines = mutable)
        _isCurrentSaved.value = false
        _toastMessage.value = "Nova fala adicionada"
    }

    fun saveCurrentScript() {
        val curr = _currentScript.value ?: return
        viewModelScope.launch {
            val id = repository.saveScript(curr)
            _currentScript.value = curr.copy(id = id.toString())
            _isCurrentSaved.value = true
            _toastMessage.value = "Roteiro salvo na sua biblioteca!"
        }
    }

    fun deleteSavedScript(script: ScriptResult) {
        viewModelScope.launch {
            repository.deleteScript(script.id)
            if (_currentScript.value?.id == script.id) {
                _isCurrentSaved.value = false
            }
            _toastMessage.value = "Roteiro excluído da biblioteca"
        }
    }

    fun copyScriptToClipboard(context: Context) {
        val curr = _currentScript.value ?: return
        val text = curr.toFormattedScreenplay()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Roteiro Rodrigo Video Dialog", text)
        clipboard.setPrimaryClip(clip)
        _toastMessage.value = "Roteiro copiado para a área de transferência!"
    }

    fun exportAndShareScript(context: Context) {
        val curr = _currentScript.value ?: return
        val text = curr.toFormattedScreenplay()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Roteiro: ${curr.title}")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(intent, "Exportar e Compartilhar Roteiro")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun dismissCurrentScript() {
        _currentScript.value = null
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
