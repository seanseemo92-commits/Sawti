package com.sawti.presentation.category

import com.sawti.domain.model.forEditing
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.ripple
import androidx.compose.foundation.background
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sawti.presentation.theme.cardElevation
import com.sawti.R
import com.sawti.domain.model.Category
import com.sawti.domain.model.CategoryIds
import com.sawti.domain.model.FamilyRelationships
import com.sawti.presentation.onboarding.spotlightAnchor
import com.sawti.domain.model.ViewMode
import com.sawti.domain.model.WordItem
import com.sawti.presentation.common.ActionPill
import com.sawti.presentation.common.HintButton
import com.sawti.presentation.common.CardImageContent
import com.sawti.presentation.common.CeramicCard
import com.sawti.presentation.common.StaggeredCascade
import com.sawti.presentation.common.bounceClick
import com.sawti.presentation.theme.CategoryPalette
import com.sawti.presentation.theme.categoryPaletteFor
import com.sawti.presentation.theme.categoryBannerColor
import kotlinx.coroutines.launch

@Composable
fun CategoryScreen(category: Category, state: LibraryUiState, actions: LibraryActions) {
    val palette = categoryPaletteFor(category.id, category.palette)
    val isFamily = category.id == CategoryIds.FAMILY
    val isCustom = category.id.startsWith("custom_")
    var addingCustomWord by rememberSaveable(category.id) { mutableStateOf(false) }
    val subgroups = if (isFamily) FamilyRelationships.tabs + category.subgroups.filter {
        it.isNotBlank() && it !in FamilyRelationships.tabs
    } else if (category.id == "cuisine") emptyList() else category.subgroups
    var mode by rememberSaveable(category.id) { mutableStateOf(ViewMode.GRID) }
    var subgroup by rememberSaveable(category.id) {
        mutableStateOf(if (isFamily) FamilyRelationships.RELATIVES else subgroups.firstOrNull().orEmpty())
    }
    LaunchedEffect(category.id, subgroups) {
        if (subgroup !in subgroups && subgroups.isNotEmpty()) subgroup = subgroups.first()
    }
    val items = if (isFamily) category.items.filter { it.subgroup == subgroup }
        else if (subgroups.isEmpty()) category.items else category.items.filter { it.subgroup == subgroup }
    val isPeopleGroup = isFamily && subgroup != FamilyRelationships.RELATIVES
    val context = LocalContext.current
    val onCall: (String) -> Unit = { phone ->
        try { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))) }
        catch (_: ActivityNotFoundException) { }
    }
    CompositionLocalProvider(LocalIndication provides ripple(color = categoryBannerColor(category.id, palette.banner))) {
        Column(Modifier.fillMaxSize()) {
            if (subgroups.isNotEmpty()) {
                SubgroupTabs(subgroups = subgroups, selected = subgroup, palette = palette,
                    onSelect = { subgroup = it; mode = ViewMode.GRID },
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp))
            }
            ViewModeToggle(mode = mode, palette = palette, onChange = { mode = it },
                modifier = Modifier.align(Alignment.End).padding(horizontal = 16.dp, vertical = 10.dp))
            Box(Modifier.weight(1f)) {
                if (isPeopleGroup && items.isEmpty()) {
                    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                        if (state.caregiverMode) Button(onClick = { actions.openAddFamilyMember(subgroup) },
                            modifier = Modifier.size(104.dp), shape = CircleShape) {
                            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.family_add), modifier = Modifier.size(48.dp))
                        }
                        if (state.caregiverMode) Spacer(Modifier.height(18.dp))
                        Text(stringResource(R.string.family_empty_title, subgroup), fontSize = 23.sp,
                            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text(stringResource(if (state.caregiverMode) R.string.family_empty_caregiver else R.string.family_empty_patient),
                            fontSize = 16.sp, textAlign = TextAlign.Center)
                    }
                } else when (mode) {
                    ViewMode.GRID -> WordGrid(items, category, palette, state, actions)
                    ViewMode.FLASHCARD -> key(subgroup) { FlashcardCarousel(items, category, palette, state, actions, onCall) }
                }
            }
            if (isCustom && state.caregiverMode) {
                Button(onClick = { addingCustomWord = true }, modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp).heightIn(min = 56.dp)) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.size(10.dp))
                    Text("إضافة بطاقة")
                }
            }
            if (isPeopleGroup && items.isNotEmpty() && state.caregiverMode) {
                Button(onClick = { actions.openAddFamilyMember(subgroup) }, modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp).heightIn(min = 56.dp)) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.size(10.dp))
                    Text(stringResource(R.string.family_add), fontSize = 18.sp)
                }
            }
        }
    }
    if (isCustom && state.caregiverMode && addingCustomWord) {
        AddCustomWordDialog(categoryTitle = category.title,
            onAdd = { title, spoken -> actions.addCustomCard(category.id, title, spoken) },
            onDismiss = { addingCustomWord = false })
    }
    if (isFamily && state.caregiverMode && state.addingFamilyGroup != null) {
        AddFamilyMemberDialog(group = state.addingFamilyGroup, actions = actions)
    }
    val preview = state.previewItemId?.let { id -> category.items.firstOrNull { it.id == id } }
    if (preview != null && !state.caregiverMode) WordPreviewDialog(preview, palette, actions, onCall)
    val focus = state.focusItemId?.let { id -> category.items.firstOrNull { it.id == id } }
    if (focus != null) WordFocusDialog(focus, palette, actions)
    val editing = state.editingItemId?.let { id -> category.items.firstOrNull { it.id == id }?.forEditing() }
    if (editing != null && state.caregiverMode) {
        WordEditorDialog(editing, isRecording = state.recordingItemId == editing.id,
            actions = actions, availableCategories = state.categories.filter { it.id.startsWith("custom_") })
    }
}

@Composable
private fun SubgroupTabs(subgroups: List<String>, selected: String, palette: CategoryPalette,
    onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.selectableGroup().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        subgroups.forEach { label ->
            val on = label == selected
            val tourModifier = when (label) {
                FamilyRelationships.RELATIVES -> Modifier.spotlightAnchor("family_relatives")
                "الزوج" -> Modifier.spotlightAnchor("family_people")
                else -> Modifier
            }
            Surface(shape = RoundedCornerShape(22.dp), color = if (on) palette.banner else MaterialTheme.colorScheme.surface,
                contentColor = if (on) Color.White else palette.accent,
                shadowElevation = cardElevation(if (on) 4.dp else 1.dp),
                modifier = Modifier.heightIn(min = 46.dp).then(tourModifier).clip(RoundedCornerShape(22.dp))
                    .selectable(selected = on, role = Role.Tab, onClick = { onSelect(label) })) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)) {
                    Text(label, fontSize = 15.sp, lineHeight = 20.sp,
                        fontWeight = FontWeight.ExtraBold, maxLines = 2, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun ViewModeToggle(mode: ViewMode, palette: CategoryPalette, onChange: (ViewMode) -> Unit, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface,
        shadowElevation = cardElevation(2.dp), modifier = modifier) {
        Row(Modifier.padding(4.dp).selectableGroup()) {
            listOf(ViewMode.GRID to (Icons.Filled.GridView to R.string.view_grid),
                ViewMode.FLASHCARD to (Icons.Filled.ViewCarousel to R.string.view_flashcards)).forEach { (m, iconAndLabel) ->
                val on = m == mode
                Box(Modifier.size(44.dp).clip(CircleShape).background(if (on) palette.banner else Color.Transparent)
                    .selectable(selected = on, role = Role.RadioButton, onClick = { onChange(m) }),
                    contentAlignment = Alignment.Center) {
                    Icon(iconAndLabel.first, contentDescription = stringResource(iconAndLabel.second),
                        tint = if (on) Color.White else palette.accent)
                }
            }
        }
    }
}

private val WordCaptionStyle = TextStyle(
    fontSize = 18.sp, lineHeight = 30.sp,
    fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center,
    platformStyle = PlatformTextStyle(includeFontPadding = true),
)

@Composable
private fun WordGrid(items: List<WordItem>, category: Category, palette: CategoryPalette,
    state: LibraryUiState, actions: LibraryActions) {
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val available = (maxWidth - 32.dp).coerceAtLeast(1.dp)
        val columns = wordGridColumnCount(available.value)
        val cellWidth = ((available - 14.dp * (columns - 1)) / columns).coerceAtLeast(1.dp)
        val captionWidthPx = with(density) { (cellWidth - 16.dp).roundToPx().coerceAtLeast(1) }
        // Measure full captions per row so long custom titles never inflate every card.
        val cardHeights = remember(items, columns, captionWidthPx, cellWidth, density.density, density.fontScale, measurer) {
            items.map { item ->
                val heightPx = measurer.measure(
                    text = item.displayText, style = WordCaptionStyle,
                    constraints = Constraints(maxWidth = captionWidthPx),
                ).size.height
                wordCardHeightDp(cellWidth.value, heightPx / density.density).dp
            }.chunked(columns).flatMap { row ->
                val tallest = row.maxOrNull() ?: 0.dp
                List(row.size) { tallest }
            }
        }
        LazyVerticalGrid(columns = GridCells.Fixed(columns), modifier = Modifier.fillMaxSize().testTag("word-grid"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                StaggeredCascade(key = "${category.id}:${item.id}", row = index / columns, modifier = Modifier.fillMaxWidth()) {
                    WordCard(item = item, palette = palette, crop = category.previewOnTap,
                        isActive = state.activeItemId == item.id,
                        isSpeaking = state.playbackVisual.isSpeaking && state.playbackVisual.activeWordId == item.id,
                        caregiverMode = state.caregiverMode,
                        onClick = { actions.onItemTapped(item, flashcard = false) },
                        onLongClick = { actions.onItemLongPressed(item) }, onHint = { actions.onHint(item) },
                        cardHeight = cardHeights[index])
                }
            }
        }
    }
}

@Composable
internal fun WordCard(item: WordItem, palette: CategoryPalette, crop: Boolean, isActive: Boolean,
    isSpeaking: Boolean = false, caregiverMode: Boolean, onClick: () -> Unit, onLongClick: () -> Unit,
    onHint: () -> Unit, cardHeight: Dp? = null) {
    val bannerColor = categoryBannerColor(item.categoryId, palette.banner)
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val height = cardHeight ?: (maxWidth / 0.85f)
        CeramicCard(isActive = isActive, isSpeaking = isSpeaking, accent = bannerColor, shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().height(height).testTag("word-card-${item.id}").clip(RoundedCornerShape(20.dp))
                .bounceClick(onLongClick = onLongClick, onClick = onClick, accent = bannerColor)) {
            Box(Modifier.fillMaxSize()) {
                CardImageContent(item = item, accent = bannerColor, isCurrentlyPlaying = isSpeaking,
                    modifier = Modifier.fillMaxSize(), crop = true, fullBleed = true)
                Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                    .background(bannerColor).padding(horizontal = 8.dp, vertical = 5.dp), contentAlignment = Alignment.Center) {
                    Text(text = item.displayText, color = Color.White, style = WordCaptionStyle)
                }
                // In the app's RTL layout TopEnd is the former speech-bubble slot.
                HintButton(onClick = onHint, modifier = Modifier.align(Alignment.TopEnd).padding(6.dp))
            }
        }
    }
}

@Composable
private fun FlashcardCarousel(items: List<WordItem>, category: Category, palette: CategoryPalette,
    state: LibraryUiState, actions: LibraryActions, onCall: (String) -> Unit) {
    if (items.isEmpty()) return
    val pager = rememberPagerState(pageCount = { items.size })
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(state = pager, modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp), pageSpacing = 16.dp, key = { items[it].id }) { page ->
            FlashcardPage(items[page], category, palette, state, actions, onCall)
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            NavCircle(enabled = pager.currentPage > 0, palette = palette, label = stringResource(R.string.previous_card),
                onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            }
            Text("${pager.currentPage + 1} / ${items.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            NavCircle(enabled = pager.currentPage < items.size - 1, palette = palette, label = stringResource(R.string.next_card),
                onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@Composable
private fun NavCircle(enabled: Boolean, palette: CategoryPalette, label: String, onClick: () -> Unit, icon: @Composable () -> Unit) {
    Surface(shape = CircleShape, color = if (enabled) palette.banner else palette.banner.copy(alpha = 0.3f),
        shadowElevation = cardElevation(if (enabled) 4.dp else 0.dp),
        modifier = Modifier.size(60.dp).semantics { contentDescription = label }
            .then(if (enabled) Modifier.bounceClick(onClick = onClick) else Modifier)) {
        Box(contentAlignment = Alignment.Center) { icon() }
    }
}

@Composable
private fun FlashcardPage(item: WordItem, category: Category, palette: CategoryPalette,
    state: LibraryUiState, actions: LibraryActions, onCall: (String) -> Unit) {
    CeramicCard(isSpeaking = state.playbackVisual.activeWordId == item.id && state.playbackVisual.isSpeaking,
        accent = palette.banner, modifier = Modifier.fillMaxSize().padding(vertical = 8.dp)) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val imageHeight = minOf(maxHeight * 0.58f, maxWidth, 360.dp)
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.fillMaxWidth().height(imageHeight).bounceClick(
                    onLongClick = { actions.onItemLongPressed(item) },
                    onClick = { actions.onItemTapped(item, flashcard = true) },
                    accent = categoryBannerColor(category.id, palette.banner)), contentAlignment = Alignment.Center) {
                    CardImageContent(item = item, accent = palette.banner,
                        isCurrentlyPlaying = state.playbackVisual.isSpeaking && state.playbackVisual.activeWordId == item.id,
                        modifier = Modifier.fillMaxSize().padding(22.dp), crop = category.previewOnTap)
                    HintButton(onClick = { actions.onHint(item) },
                        modifier = Modifier.align(Alignment.TopEnd).padding(6.dp))
                }
                Text(item.displayText, color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 34.sp, lineHeight = 44.sp,
                    fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                BoxWithConstraints(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 18.dp)) {
                    val compact = maxWidth < 380.dp || LocalDensity.current.fontScale > 1.3f
                    val buttons: @Composable () -> Unit = {
                        ActionPill(label = stringResource(R.string.listen),
                            icon = Icons.AutoMirrored.Filled.VolumeUp, color = palette.banner,
                            onClick = { actions.onSpeakAgain(item) },
                            modifier = Modifier.testTag("flashcard-listen-${item.id}"))
                        if (item.phone.isNotBlank() && !state.caregiverMode) {
                            ActionPill(label = stringResource(R.string.call),
                                icon = Icons.Filled.Call, color = palette.banner,
                                onClick = { onCall(item.phone) })
                        }
                    }
                    if (compact) {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)) { buttons() }
                    } else {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) { buttons() }
                    }
                }
            }
        }
    }
}
