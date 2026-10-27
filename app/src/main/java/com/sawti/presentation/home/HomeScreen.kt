package com.sawti.presentation.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sawti.R
import com.sawti.domain.model.Category
import com.sawti.domain.model.WordItem
import com.sawti.presentation.category.FavoriteEntry
import com.sawti.presentation.common.CardImageContent
import com.sawti.presentation.common.CardLabel
import com.sawti.presentation.common.CeramicCard
import com.sawti.presentation.common.StaggeredCascade
import com.sawti.presentation.common.bounceClick
import com.sawti.presentation.theme.CategoryPalette
import com.sawti.presentation.theme.HintAmber
import com.sawti.presentation.theme.paletteOf

/**
 * Image-stage aspect for category cards. With a 0.88 card ratio this keeps the
 * visual stage at roughly 62%-65% of the card's height, while the label area
 * wraps its content so large OS font scales never clip Arabic diacritics.
 */
private const val CategoryStageAspect = 1.42f

@Composable
fun HomeScreen(
    categories: List<Category>,
    favorites: List<FavoriteEntry>,
    activeItemId: Long?,
    speakingItemId: Long?,
    caregiverMode: Boolean,
    onOpen: (String) -> Unit,
    onQuickTap: (WordItem) -> Unit,
    onQuickLongTap: (WordItem) -> Unit,
) {
    if (categories.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val estimatedColumns = ((LocalConfiguration.current.screenWidthDp - 32) / 160).coerceAtLeast(1)
    Column(Modifier.fillMaxSize()) {
        if (favorites.isNotEmpty() || caregiverMode) {
            FavoritesBar(favorites, activeItemId, speakingItemId, caregiverMode, onQuickTap, onQuickLongTap)
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            itemsIndexed(categories, key = { _, category -> category.id }) { index, category ->
                StaggeredCascade(row = index / estimatedColumns, modifier = Modifier.fillMaxWidth()) {
                    CategoryCard(category) { onOpen(category.id) }
                }
            }
        }
    }
}

@Composable
private fun FavoritesBar(
    favorites: List<FavoriteEntry>,
    activeItemId: Long?,
    speakingItemId: Long?,
    caregiverMode: Boolean,
    onTap: (WordItem) -> Unit,
    onLongTap: (WordItem) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(top = 14.dp)) {
        Row(
            Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = HintAmber, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                stringResource(R.string.favorites_title),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (favorites.isEmpty()) {
            Text(
                stringResource(R.string.favorites_empty_hint),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(favorites, key = { it.item.id }) { entry ->
                    FavoriteChip(
                        entry = entry,
                        isActive = activeItemId == entry.item.id,
                        isSpeaking = speakingItemId == entry.item.id,
                        onClick = { onTap(entry.item) },
                        onLongClick = { onLongTap(entry.item) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteChip(
    entry: FavoriteEntry,
    isActive: Boolean,
    isSpeaking: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val palette = paletteOf(entry.palette)
    CeramicCard(
        accent = palette.banner,
        isActive = isActive,
        isSpeaking = isSpeaking,
        modifier = Modifier
            .width(140.dp)
            .height(175.dp)
            .bounceClick(onLongClick = onLongClick, onClick = onClick),
    ) {
        Column(Modifier.fillMaxSize()) {
            CardImageContent(
                item = entry.item,
                accent = palette.banner,
                isCurrentlyPlaying = isSpeaking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
                    .padding(6.dp),
                crop = entry.item.categoryId == "family",
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                CardLabel(
                    text = entry.item.displayText,
                    fontSize = 15.sp,
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(category: Category, onClick: () -> Unit) {
    val palette = paletteOf(category.palette)
    CeramicCard(
        accent = palette.banner,
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(onClick = onClick),
    ) {
        Column(Modifier.fillMaxWidth()) {
            CategoryHeroVisual(
                category = category,
                palette = palette,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(CategoryStageAspect)
                    .padding(8.dp),
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                CardLabel(
                    text = category.title,
                    fontSize = 18.sp,
                )
            }
        }
    }
}

/**
 * Dedicated, dignified symbol per category id. It never falls back to the first
 * Arabic letter of the title or to the first item of the category.
 */
@Composable
private fun CategoryHeroVisual(
    category: Category,
    palette: CategoryPalette,
    modifier: Modifier = Modifier,
) {
    val symbol = categoryHeroSymbol(category.id)
    val heroItem = remember(category.id, category.title, symbol) {
        WordItem(
            id = -1L,
            categoryId = category.id,
            displayText = category.title,
            speechText = category.title,
            emoji = symbol,
        )
    }
    CardImageContent(
        item = heroItem,
        accent = palette.accent,
        isCurrentlyPlaying = false,
        modifier = modifier,
    )
}

private fun categoryHeroSymbol(categoryId: String): String = when (categoryId) {
    "family" -> "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66" // family
    "daily" -> "\uD83D\uDE4B" // person raising a hand: requests
    "fruit" -> "\uD83C\uDF4E" // red apple
    "vegetables" -> "\uD83E\uDD66" // broccoli
    "food" -> "\uD83C\uDF7D\uFE0F" // plate with cutlery
    "clothing" -> "\uD83D\uDC54" // necktie / formal shirt
    "home" -> "\uD83C\uDFE0" // house
    "actions" -> "\uD83C\uDFC3" // person walking briskly
    "nature" -> "\uD83C\uDF3F" // herb
    "body" -> "\u2764\uFE0F" // heart: health
    "cuisine" -> "\uD83C\uDF72" // pot of food
    "routine" -> "\uD83D\uDDD3\uFE0F" // calendar
    else -> "\uD83D\uDCC1" // generic folder for custom categories
}
