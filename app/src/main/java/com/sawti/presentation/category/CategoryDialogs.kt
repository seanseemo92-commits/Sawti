package com.sawti.presentation.category

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.MaterialTheme
import com.sawti.presentation.theme.cardElevation
import com.sawti.R
import com.sawti.domain.model.Category
import com.sawti.domain.model.CategoryIds
import com.sawti.domain.model.FamilyRelationships
import com.sawti.domain.model.WordItem
import com.sawti.presentation.common.ActionPill
import com.sawti.presentation.common.HintButton
import com.sawti.presentation.common.PhraseHintButton
import com.sawti.presentation.common.ItemImage
import com.sawti.presentation.common.VoiceRecordingSection
import com.sawti.presentation.theme.CategoryPalette
import com.sawti.presentation.theme.paletteOf

/** Family tap: the photo shown large while the name is spoken (the ViewModel starts the speech). */
@Composable
fun WordPreviewDialog(
    item: WordItem,
    palette: CategoryPalette,
    actions: LibraryActions,
    onCall: (String) -> Unit,
) {
    Dialog(
        onDismissRequest = actions::closePreview,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = cardElevation(16.dp),
            modifier = Modifier.fillMaxWidth(0.92f),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ItemImage(
                    item = item,
                    accent = palette.banner,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(palette.pastel)
                        .padding(14.dp),
                    crop = true,
                    glyphScale = 0.5f,
                )
                Text(
                    item.displayText,
                    color = palette.accent,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                )
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    HintButton(onClick = { actions.onHint(item) }, size = 60)
                    if (!item.sentencePrompt.isNullOrBlank()) {
                        PhraseHintButton(onClick = { actions.onSentenceHint(item) }, size = 60)
                    }
                    ActionPill(
                        label = stringResource(R.string.listen),
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        color = palette.banner,
                        onClick = { actions.onSpeakAgain(item) },
                    )
                    if (item.phone.isNotBlank()) {
                        ActionPill(
                            label = stringResource(R.string.call),
                            icon = Icons.Filled.Call,
                            color = palette.banner,
                            onClick = { onCall(item.phone) },
                        )
                    }
                }
                TextButton(onClick = actions::closePreview, modifier = Modifier.heightIn(min = 52.dp)) {
                    Text(stringResource(R.string.close), fontSize = 18.sp)
                }
            }
        }
    }
}

/**
 * Distraction-free focus view: only the picture, a big title and an audio replay button.
 * Opened by long-pressing a card in the grid.
 */
@Composable
fun WordFocusDialog(item: WordItem, palette: CategoryPalette, actions: LibraryActions) {
    Dialog(
        onDismissRequest = actions::closeFocus,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize()) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    ItemImage(
                        item = item,
                        accent = palette.banner,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(32.dp))
                            .background(palette.pastel)
                            .padding(20.dp),
                        crop = false,
                        glyphScale = 0.62f,
                    )
                    Text(
                        item.displayText,
                        color = palette.accent,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                    ActionPill(
                        label = stringResource(R.string.listen),
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        color = palette.banner,
                        onClick = { actions.onSpeakAgain(item) },
                    )
                }
                IconButton(
                    onClick = actions::closeFocus,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .systemBarsPadding()
                        .padding(8.dp)
                        .size(52.dp),
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.focus_close),
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
        }
    }
}

/** A family starts empty. A caregiver chooses the name and can then attach all usual media. */
@Composable
fun AddFamilyMemberDialog(group: String, actions: LibraryActions) {
    var name by remember(group) { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = actions::closeAddFamilyMember,
        title = { Text(stringResource(R.string.family_add)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it.take(100) },
                    label = { Text(stringResource(R.string.family_name)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                Text(group, style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(value = phone, onValueChange = { phone = normalizePhone(it).take(40) },
                    label = { Text(stringResource(R.string.field_phone)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { actions.addFamilyMember(name.trim(), group.trim(), phone.trim()) },
                enabled = name.isNotBlank()) { Text(stringResource(R.string.family_add_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = actions::closeAddFamilyMember) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

/** Caregiver Mode editor: label, TTS text, cue, phone (family), photo and voice recording. */
@Composable
fun WordEditorDialog(item: WordItem, isRecording: Boolean, actions: LibraryActions, availableCategories: List<Category> = emptyList()) {
    var display by remember(item.id) { mutableStateOf(item.displayText) }
    var tts by remember(item.id) { mutableStateOf(item.ttsPrompt) }
    var cue by remember(item.id) { mutableStateOf(item.phonemicCue) }
    var sentence by remember(item.id) { mutableStateOf(item.sentencePrompt.orEmpty()) }
    var phone by remember(item.id) { mutableStateOf(item.phone) }
    var group by remember(item.id) { mutableStateOf(item.subgroup) }
    var showDeleteConfirm by remember(item.id) { mutableStateOf(false) }
    var pendingMove by remember(item.id) { mutableStateOf<String?>(null) }
    val isRelationship = item.categoryId == CategoryIds.FAMILY &&
        item.subgroup == FamilyRelationships.RELATIVES
    val palette = paletteOf(if (item.categoryId == CategoryIds.FAMILY) 0 else 1)

    // System photo picker: no storage permission needed.
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) actions.onPhotoPicked(item.id, uri)
    }

    AlertDialog(
        onDismissRequest = actions::closeEditor,
        title = { Text(item.displayText, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = display, onValueChange = { display = it },
                    readOnly = isRelationship,
                    label = { Text(stringResource(R.string.field_display)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = tts, onValueChange = { tts = it },
                    label = { Text(stringResource(R.string.field_tts)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = cue, onValueChange = { cue = it },
                    label = { Text(stringResource(R.string.field_cue)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = sentence, onValueChange = { sentence = it },
                    label = { Text(stringResource(R.string.field_sentence)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (item.categoryId == CategoryIds.FAMILY) {
                    if (!isRelationship) Text(group, style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = phone, onValueChange = { phone = normalizePhone(it) },
                        label = { Text(stringResource(R.string.field_phone)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // Photo
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ItemImage(
                        item = item,
                        accent = palette.banner,
                        modifier = Modifier
                            .size(84.dp)
                            .background(palette.pastel, RoundedCornerShape(16.dp)),
                        crop = true,
                        glyphScale = 0.5f,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = {
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            modifier = Modifier.heightIn(min = 52.dp),
                        ) {
                            Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.choose_photo))
                        }
                        if (item.photoPath != null) {
                            TextButton(onClick = { actions.removePhoto(item.id) }) {
                                Text(stringResource(R.string.remove_photo))
                            }
                        }
                    }
                }

                // Real voice, replaces the robotic TTS for this card
                VoiceRecordingSection(
                    hasRecording = item.audioPath != null,
                    isRecording = isRecording,
                    onStart = { actions.startRecording(item.id) },
                    onStop = { actions.stopRecording(item.id) },
                    onPlay = { actions.previewRecording(item.id) },
                    onDelete = { actions.deleteRecording(item.id) },
                    onPickFile = { uri -> actions.onAudioPicked(item.id, uri) },
                )
                if (!isRelationship && availableCategories.any { it.id != item.categoryId }) {
                    Text("نقل البطاقة إلى قسم مخصص (احفظ أي تعديل بالنص أولاً):")
                    availableCategories.filter { it.id != item.categoryId }.forEach { target ->
                        TextButton(
                            onClick = { pendingMove = target.id },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("نقل إلى «${target.title}»") }
                    }
                }
                if (item.categoryId == CategoryIds.FAMILY && !isRelationship) {
                    TextButton(onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.family_delete))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { actions.saveDetails(item.id, display.trim(), tts.trim(), cue.trim(), sentence.trim(), phone.trim(), group.trim()) },
                modifier = Modifier.heightIn(min = 52.dp),
            ) { Text(stringResource(R.string.save), fontSize = 18.sp) }
        },
        dismissButton = {
            TextButton(onClick = actions::closeEditor, modifier = Modifier.heightIn(min = 52.dp)) {
                Text(stringResource(R.string.cancel), fontSize = 18.sp)
            }
        },
    )
    val target = availableCategories.firstOrNull { it.id == pendingMove }
    if (target != null) {
        AlertDialog(
            onDismissRequest = { pendingMove = null },
            title = { Text("تأكيد نقل البطاقة") },
            text = { Text("نقل «${item.displayText}» إلى «${target.title}»؟ تبقى الصور والتسجيلات والمفضلة محفوظة. احفظ تعديل النص أولًا إن لزم.") },
            confirmButton = {
                Button(onClick = {
                    actions.moveCardToCustomCategory(item.id, target.id)
                    pendingMove = null
                }) { Text("نقل") }
            },
            dismissButton = { TextButton(onClick = { pendingMove = null }) { Text("إلغاء") } },
        )
    }
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.family_delete)) },
            text = { Text(stringResource(R.string.family_delete_confirm, item.displayText)) },
            confirmButton = {
                Button(onClick = { showDeleteConfirm = false; actions.deleteFamilyMember(item.id) }) {
                    Text(stringResource(R.string.family_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

/** Keeps digits, + - ( ) and spaces; converts Arabic-Indic digits so the dialer always gets 0-9. */
private fun normalizePhone(raw: String): String = buildString {
    raw.forEach { c ->
        when (c) {
            in '0'..'9', '+', '-', ' ', '(', ')' -> append(c)
            in '\u0660'..'\u0669' -> append('0' + (c - '\u0660'))
        }
    }
}
