package com.sawti.presentation.category

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import com.sawti.presentation.theme.cardElevation
import com.sawti.R
import com.sawti.domain.model.Category
import com.sawti.domain.model.CategoryIds
import com.sawti.domain.model.FamilyRelationships
import com.sawti.domain.model.ViewMode
import com.sawti.domain.model.WordItem
import com.sawti.presentation.common.ActionPill
import com.sawti.presentation.common.EditBadge
import com.sawti.presentation.common.FavoriteStar
import com.sawti.presentation.common.HintButton
import com.sawti.presentation.common.PhraseHintButton
import com.sawti.presentation.common.ItemImage
import com.sawti.presentation.common.bounceClick
import com.sawti.presentation.theme.CategoryPalette
import com.sawti.presentation.theme.paletteOf
import kotlinx.coroutines.launch

@Composable
fun CategoryScreen(category: Category, state: LibraryUiState, actions: LibraryActions) {
    val palette = paletteOf(category.palette)
    val isFamily = category.id == CategoryIds.FAMILY
    val subgroups = if (isFamily) FamilyRelationships.tabs + category.subgroups.filter {
        it.isNotBlank() && it !in FamilyRelationships.tabs
    } else category.subgroups
    var mode by rememberSaveable(category.id) { mutableStateOf(ViewMode.GRID) }
    var subgroup by rememberSaveable(category.id) {
        mutableStateOf(if (isFamily) FamilyRelationships.RELATIVES else subgroups.firstOrNull().orEmpty())
    }
    LaunchedEffect(category.id, subgroups) {
        if (subgroup !in subgroups && subgroups.isNotEmpty()) subgroup = subgroups.first()
    }
    val items = if (isFamily) category.items.filter { it.subgroup == subgroup }
        else if (subgroups.isEmpty()) category.items
        else category.items.filter { it.subgroup == subgroup }
    val isPeopleGroup = isFamily && subgroup != FamilyRelationships.RELATIVES

    val context = LocalContext.current
    val onCall: (String) -> Unit = { phone ->
        try {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null)))
        } catch (_: ActivityNotFoundException) { /* no dialer on this device */ }
    }

    Column(Modifier.fillMaxSize()) {
        // Always show the relationship vocabulary first; person groups exist even when empty.
        if (subgroups.isNotEmpty()) {
            SubgroupTabs(
                subgroups = subgroups,
                selected = subgroup,
                palette = palette,
                onSelect = { subgroup = it; mode = ViewMode.GRID },
                modifier = Modifier.fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            )
        }
        ViewModeToggle(
            mode = mode,
            palette = palette,
            onChange = { mode = it },
            modifier = Modifier
                .align(Alignment.End)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        )
        Box(Modifier.weight(1f)) {
            if (isPeopleGroup && items.isEmpty()) {
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (state.caregiverMode) Button(
                        onClick = { actions.openAddFamilyMember(subgroup) },
                        modifier = Modifier.size(104.dp),
                        shape = CircleShape,
                    ) { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.family_add),
                        modifier = Modifier.size(48.dp)) }
                    if (state.caregiverMode) Spacer(Modifier.height(18.dp))
                    Text(stringResource(R.string.family_empty_title, subgroup), fontSize = 23.sp,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text(stringResource(if (state.caregiverMode) R.string.family_empty_caregiver else R.string.family_empty_patient), fontSize = 16.sp,
                        textAlign = TextAlign.Center)
                }
            } else when (mode) {
                ViewMode.GRID -> WordGrid(items, category, palette, state, actions)
                ViewMode.FLASHCARD -> key(subgroup) {
                    FlashcardCarousel(items, category, palette, state, actions, onCall)
                }
            }
        }
        if (isPeopleGroup && items.isNotEmpty() && state.caregiverMode) {
            Button(onClick = { actions.openAddFamilyMember(subgroup) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                    .heightIn(min = 56.dp)) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.size(10.dp))
                Text(stringResource(R.string.family_add), fontSize = 18.sp)
            }
        }
    }

    if (isFamily && state.caregiverMode && state.addingFamilyGroup != null) {
        AddFamilyMemberDialog(group = state.addingFamilyGroup, actions = actions)
    }

    // Large photo for family members (auto-speaks when opened).
    val preview = state.previewItemId?.let { id -> category.items.firstOrNull { it.id == id } }
    if (preview != null && !state.caregiverMode) {
        WordPreviewDialog(preview, paletteOf(category.palette), actions, onCall)
    }
    // Distraction-free focus view (long-press on a card).
    val focus = state.focusItemId?.let { id -> category.items.firstOrNull { it.id == id } }
    if (focus != null) {
        WordFocusDialog(focus, paletteOf(category.palette), actions)
    }
    // Caregiver editor.
    val editing = state.editingItemId?.let { id -> category.items.firstOrNull { it.id == id } }
    if (editing != null && state.caregiverMode) {
        WordEditorDialog(editing, isRecording = state.recordingItemId == editing.id, actions = actions)
    }
}

// ------------------------------------------------------------------ controls

@Composable
private fun SubgroupTabs(
    subgroups: List<String>,
    selected: String,
    palette: CategoryPalette,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        subgroups.forEach { label ->
            val on = label == selected
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = if (on) palette.banner else MaterialTheme.colorScheme.surface,
                contentColor = if (on) Color.White else palette.accent,
                shadowElevation = cardElevation(if (on) 4.dp else 1.dp),
                modifier = Modifier
                    .height(46.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .clickable { onSelect(label) },
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 18.dp)) {
                    Text(label,
                        fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun ViewModeToggle(
    mode: ViewMode,
    palette: CategoryPalette,
    onChange: (ViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = cardElevation(2.dp), modifier = modifier) {
        Row(Modifier.padding(4.dp)) {
            listOf(
                ViewMode.GRID to (Icons.Filled.GridView to R.string.view_grid),
                ViewMode.FLASHCARD to (Icons.Filled.ViewCarousel to R.string.view_flashcards),
            ).forEach { (m, iconAndLabel) ->
                val on = m == mode
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (on) palette.banner else Color.Transparent)
                        .clickable { onChange(m) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        iconAndLabel.first,
                        contentDescription = stringResource(iconAndLabel.second),
                        tint = if (on) Color.White else palette.accent,
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------------ grid

@Composable
private fun WordGrid(
    items: List<WordItem>,
    category: Category,
    palette: CategoryPalette,
    state: LibraryUiState,
    actions: LibraryActions,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 28.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(items, key = { it.id }) { item ->
            WordCard(
                item = item,
                palette = palette,
                crop = category.previewOnTap,
                isActive = state.activeItemId == item.id,
                caregiverMode = state.caregiverMode,
                onClick = { actions.onItemTapped(item, flashcard = false) },
                onLongClick = { actions.onItemLongPressed(item) },
                onHint = { actions.onHint(item) },
                onSentenceHint = { actions.onSentenceHint(item) },
                onToggleFavorite = { actions.onToggleFavorite(item) },
            )
        }
    }
}

@Composable
private fun WordCard(
    item: WordItem,
    palette: CategoryPalette,
    crop: Boolean,
    isActive: Boolean,
    caregiverMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onHint: () -> Unit,
    onSentenceHint: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = cardElevation(if (isActive) 12.dp else 4.dp),
        border = if (isActive) BorderStroke(3.dp, palette.banner) else null,
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(onLongClick = onLongClick, onClick = onClick),
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.1f)
                    .background(palette.pastel),
            ) {
                ItemImage(
                    item = item,
                    accent = palette.banner,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    crop = crop,
                )
                // 💡 first-sound cue, 💬 sentence cue (only when the card has one)
                Column(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    HintButton(onClick = onHint, size = 40)
                    if (!item.sentencePrompt.isNullOrBlank()) PhraseHintButton(onClick = onSentenceHint, size = 40)
                }
                if (caregiverMode) {
                    EditBadge(Modifier.align(Alignment.TopStart).padding(8.dp))
                    FavoriteStar(
                        isFavorite = item.isFavorite,
                        onClick = onToggleFavorite,
                        modifier = Modifier.align(Alignment.BottomStart).padding(6.dp),
                    )
                }
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .background(palette.banner)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    item.displayText,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
        }
    }
}

// ------------------------------------------------------------------ flashcards

@Composable
private fun FlashcardCarousel(
    items: List<WordItem>,
    category: Category,
    palette: CategoryPalette,
    state: LibraryUiState,
    actions: LibraryActions,
    onCall: (String) -> Unit,
) {
    if (items.isEmpty()) return
    val pager = rememberPagerState(pageCount = { items.size })
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pager,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp),
            pageSpacing = 16.dp,
            key = { items[it].id },
        ) { page ->
            FlashcardPage(items[page], category, palette, state, actions, onCall)
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            NavCircle(
                enabled = pager.currentPage > 0,
                palette = palette,
                label = stringResource(R.string.previous_card),
                onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } },
            ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White) }

            Text("${pager.currentPage + 1} / ${items.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            NavCircle(
                enabled = pager.currentPage < items.size - 1,
                palette = palette,
                label = stringResource(R.string.next_card),
                onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
            ) { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White) }
        }
    }
}

@Composable
private fun NavCircle(
    enabled: Boolean,
    palette: CategoryPalette,
    label: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    Surface(
        shape = CircleShape,
        color = if (enabled) palette.banner else palette.banner.copy(alpha = 0.3f),
        shadowElevation = cardElevation(if (enabled) 4.dp else 0.dp),
        modifier = Modifier
            .size(60.dp)
            .semantics { contentDescription = label }
            .then(if (enabled) Modifier.bounceClick(onClick = onClick) else Modifier),
    ) {
        Box(contentAlignment = Alignment.Center) { icon() }
    }
}

@Composable
private fun FlashcardPage(
    item: WordItem,
    category: Category,
    palette: CategoryPalette,
    state: LibraryUiState,
    actions: LibraryActions,
    onCall: (String) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = cardElevation(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(palette.pastel)
                    .clickable { actions.onItemTapped(item, flashcard = true) },
                contentAlignment = Alignment.Center,
            ) {
                ItemImage(
                    item = item,
                    accent = palette.banner,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    crop = category.previewOnTap,
                    glyphScale = 0.6f,
                )
                if (state.caregiverMode) {
                    EditBadge(Modifier.align(Alignment.TopStart).padding(14.dp))
                    FavoriteStar(
                        isFavorite = item.isFavorite,
                        onClick = { actions.onToggleFavorite(item) },
                        modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
                    )
                }
            }
            Text(
                item.displayText,
                color = palette.accent,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
            Row(
                Modifier.padding(start = 16.dp, end = 16.dp, bottom = 18.dp),
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
                if (item.phone.isNotBlank() && !state.caregiverMode) {
                    ActionPill(
                        label = stringResource(R.string.call),
                        icon = Icons.Filled.Call,
                        color = palette.banner,
                        onClick = { onCall(item.phone) },
                    )
                }
            }
        }
    }
}
