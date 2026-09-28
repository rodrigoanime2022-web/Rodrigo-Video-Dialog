package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DialogueLine
import com.example.data.model.ScriptResult
import com.example.ui.theme.RodrigoAmber
import com.example.ui.theme.RodrigoAmberDark
import com.example.ui.theme.RodrigoAmberLight
import com.example.ui.theme.RodrigoContainerDark

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ScriptResultScreen(
    script: ScriptResult,
    isSaved: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onCopy: (Context) -> Unit,
    onExport: (Context) -> Unit,
    onUpdateTitle: (String) -> Unit,
    onUpdateLine: (lineId: String, newSpeaker: String, newText: String, newAction: String, isRodrigo: Boolean) -> Unit,
    onDeleteLine: (lineId: String) -> Unit,
    onAddLine: (afterIndex: Int, isRodrigo: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { onBack() }

    // State for editing a specific dialogue line
    var editingLine by remember { mutableStateOf<DialogueLine?>(null) }
    var isEditingTitle by remember { mutableStateOf(false) }
    var tempTitle by remember(script.title) { mutableStateOf(script.title) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("script_result_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Roteiro Gerado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = script.sceneHeader,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("result_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar para o início"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onCopy(context) },
                        modifier = Modifier.testTag("action_copy_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar roteiro")
                    }
                    IconButton(
                        onClick = onSave,
                        modifier = Modifier.testTag("action_save_button")
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isSaved) "Roteiro salvo" else "Salvar roteiro",
                            tint = if (isSaved) RodrigoAmber else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { onExport(context) },
                        modifier = Modifier.testTag("action_export_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Exportar e compartilhar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Copy button
                    OutlinedButton(
                        onClick = { onCopy(context) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("bottom_copy_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copiar", maxLines = 1)
                    }

                    // Save button
                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSaved) RodrigoAmber else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("bottom_save_button")
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isSaved) Color.Black else Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSaved) "Salvo!" else "Salvar",
                            color = if (isSaved) Color.Black else Color.White,
                            maxLines = 1
                        )
                    }

                    // Export button
                    Button(
                        onClick = { onExport(context) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("bottom_export_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Exportar", maxLines = 1)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Title and Scene Info Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = script.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { isEditingTitle = true }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Editar título",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = script.sceneHeader,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        if (script.synopsis.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = script.synopsis,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Characters in scene
                        Text(
                            text = "Personagens na cena:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            script.characters.forEach { charName ->
                                val isRodrigo = charName.contains("Rodrigo", ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isRodrigo) RodrigoAmber.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isRodrigo) RodrigoAmber else MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        if (isRodrigo) {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = null,
                                                tint = RodrigoAmberDark,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = charName,
                                            fontSize = 12.sp,
                                            fontWeight = if (isRodrigo) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isRodrigo) RodrigoAmberDark else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Rodrigo Highlight Banner
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = RodrigoAmber.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, RodrigoAmber.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(RodrigoAmber),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Entrada do Personagem Adicional",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = RodrigoAmberDark
                            )
                            Text(
                                text = "Rodrigo entra organicamente na cena. Toque no lápis em qualquer linha para editar o roteiro.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Screenplay Dialogue Lines
            itemsIndexed(script.lines, key = { _, line -> line.id }) { index, line ->
                DialogueLineCard(
                    line = line,
                    onEdit = { editingLine = line },
                    onDelete = { onDeleteLine(line.id) },
                    onAddBelow = { isRodrigo -> onAddLine(index, isRodrigo) }
                )
            }

            // Bottom spacer so content doesn't get hidden behind bottom bar
            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    // Dialog for Editing Title
    if (isEditingTitle) {
        AlertDialog(
            onDismissRequest = { isEditingTitle = false },
            title = { Text("Editar Título do Roteiro") },
            text = {
                OutlinedTextField(
                    value = tempTitle,
                    onValueChange = { tempTitle = it },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (tempTitle.isNotBlank()) {
                        onUpdateTitle(tempTitle)
                    }
                    isEditingTitle = false
                }) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditingTitle = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog for Editing a Line
    editingLine?.let { lineToEdit ->
        EditLineDialog(
            line = lineToEdit,
            onDismiss = { editingLine = null },
            onConfirm = { updatedSpeaker, updatedText, updatedAction, isRodrigo ->
                onUpdateLine(lineToEdit.id, updatedSpeaker, updatedText, updatedAction, isRodrigo)
                editingLine = null
            }
        )
    }
}

@Composable
fun DialogueLineCard(
    line: DialogueLine,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddBelow: (isRodrigo: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dialogue_line_${line.id}"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            if (line.isRodrigo) 2.dp else 1.dp,
            if (line.isRodrigo) RodrigoAmber else MaterialTheme.colorScheme.outlineVariant
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (line.isRodrigo) RodrigoAmber.copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (line.isRodrigo) 3.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Speaker row & actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (line.isRodrigo) RodrigoAmber else MaterialTheme.colorScheme.primary
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (line.isRodrigo) Icons.Default.Star else Icons.Default.Person,
                            contentDescription = null,
                            tint = if (line.isRodrigo) Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = line.speaker.uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (line.isRodrigo) RodrigoAmberDark else MaterialTheme.colorScheme.onSurface
                    )

                    if (line.isRodrigo) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = RodrigoAmber
                        ) {
                            Text(
                                text = "RODRIGO",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (line.timestamp.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "[${line.timestamp}]",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar fala",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Excluir fala",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Rubric / Action note
            if (line.action.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "(${line.action})",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Dialogue Spoken Text
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (line.isRodrigo) RodrigoAmber.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "\"${line.text}\"",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(12.dp)
                )
            }

            // Quick add buttons below
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = { onAddBelow(false) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Fala", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(
                    onClick = { onAddBelow(true) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = RodrigoAmber, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Rodrigo", fontSize = 11.sp, color = RodrigoAmberDark)
                }
            }
        }
    }
}

@Composable
fun EditLineDialog(
    line: DialogueLine,
    onDismiss: () -> Unit,
    onConfirm: (speaker: String, text: String, action: String, isRodrigo: Boolean) -> Unit
) {
    var speaker by remember { mutableStateOf(line.speaker) }
    var text by remember { mutableStateOf(line.text) }
    var action by remember { mutableStateOf(line.action) }
    var isRodrigo by remember { mutableStateOf(line.isRodrigo) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Linha do Diálogo") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = speaker,
                    onValueChange = {
                        speaker = it
                        if (it.contains("Rodrigo", ignoreCase = true)) {
                            isRodrigo = true
                        }
                    },
                    label = { Text("Personagem / Quem fala") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = action,
                    onValueChange = { action = it },
                    label = { Text("Rubrica de Ação (opcional)") },
                    placeholder = { Text("Ex: olhando surpreso para a porta") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Fala / Diálogo") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "É o Rodrigo?",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = isRodrigo,
                        onCheckedChange = { isRodrigo = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = RodrigoAmber,
                            checkedTrackColor = RodrigoAmber.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(speaker, text, action, isRodrigo)
            }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
