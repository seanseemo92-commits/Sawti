package com.sawti.presentation.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sawti.domain.model.Category
import com.sawti.domain.model.WordItem
import com.sawti.domain.search.ArabicSearch
import com.sawti.presentation.theme.paletteOf

/** Global search: results are the same spoken/editable WordCards as category grids. */
@Composable
fun SearchResultsScreen(
    categories: List<Category>,
    searchQuery: String,
    state: LibraryUiState,
    actions: LibraryActions,
    onOpenResult: (WordItem) -> Unit,
) {
    val results = categories.flatMap { category ->
        category.items.filter {
            ArabicSearch.matches(it.displayText, it.ttsPrompt, searchQuery)
        }.map { category to it }
    }
    if (results.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("ما لقيناش كلمات تطابق البحث")
        }
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(results, key = { it.second.id }) { (category, item) ->
            Column {
                Text(category.title, modifier = Modifier.padding(bottom = 5.dp))
                WordCard(
                    item = item,
                    palette = paletteOf(category.palette),
                    crop = category.previewOnTap,
                    isActive = state.activeItemId == item.id,
                    isSpeaking = state.playbackVisual.isSpeaking && state.playbackVisual.activeWordId == item.id,
                    caregiverMode = state.caregiverMode,
                    onClick = { onOpenResult(item) },
                    onLongClick = { actions.onItemLongPressed(item) },
                    onHint = { actions.onHint(item) },
                    onSentenceHint = { actions.onSentenceHint(item) },
                    onToggleFavorite = { actions.onToggleFavorite(item) },
                )
            }
        }
    }
}
