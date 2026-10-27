package com.sawti.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sawti.presentation.theme.cardElevation
import com.sawti.R
import com.sawti.domain.model.Category
import com.sawti.domain.model.WordItem
import com.sawti.presentation.category.FavoriteEntry
import com.sawti.presentation.common.ItemImage
import com.sawti.presentation.common.CardImageContent
import com.sawti.presentation.common.CeramicCard
import com.sawti.presentation.common.CeramicImageTint
import com.sawti.presentation.common.VoiceAura
import com.sawti.presentation.common.StaggeredCascade
import com.sawti.presentation.common.bounceClick
import com.sawti.presentation.theme.CategoryPalette
import com.sawti.presentation.theme.HintAmber
import com.sawti.presentation.theme.paletteOf

/**
 * Home: quick-access favourites row on top, then an adaptive category grid
 * (first two cards are always عائلتي and طلباتي اليومية).
 */
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
    // Match the adaptive grid's effective column count for staggered *rows*.
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
                StaggeredCascade(key = category.id, row = index / estimatedColumns, modifier = Modifier.fillMaxWidth()) {
                    CategoryCard(category) { onOpen(category.id) }
                }
            }
        }
    }
}

// ------------------------------------------------------------------ quick access

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
                    FavoriteChip(entry, isActive = activeItemId == entry.item.id, isSpeaking = speakingItemId == entry.item.id, onClick = { onTap(entry.item) }, onLongClick = { onLongTap(entry.item) })
                }
            }
        }
    }
}

/** One pinned card: tap = instant speech. */
@Composable
private fun FavoriteChip(entry: FavoriteEntry, isActive: Boolean, isSpeaking: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val palette = paletteOf(entry.palette)
    CeramicCard(
        isActive = isActive,
        isSpeaking = isSpeaking,
        accent = palette.banner,
        modifier = Modifier
            .width(144.dp)
            .height(180.dp)
            .bounceClick(onLongClick = onLongClick, onClick = onClick),
    ) {
        Column {
            CardImageContent(
                item = entry.item,
                accent = palette.banner,
                isCurrentlyPlaying = isSpeaking,
                modifier = Modifier.height(116.dp).fillMaxWidth().padding(6.dp),
                crop = entry.item.categoryId == "family",
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    entry.item.displayText,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ------------------------------------------------------------------ categories

@Composable
private fun CategoryCard(category: Category, onClick: () -> Unit) {
    val palette = paletteOf(category.palette)
    CeramicCard(
        accent = palette.banner,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 210.dp)
            .bounceClick(onClick = onClick),
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(148.dp), contentAlignment = Alignment.Center) {
                // Use authentic user photos as the hero. With none attached,
                // display a quiet category monogram instead of an oversized emoji.
                val hero = category.items.firstOrNull { it.photoPath != null }
                if (hero != null) {
                    CardImageContent(
                        item = hero,
                        accent = palette.banner,
                        isCurrentlyPlaying = false,
                        modifier = Modifier.fillMaxSize().padding(10.dp),
                        crop = category.previewOnTap,
                    )
                } else {
                    Box(
                        Modifier.size(88.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.56f),
                                RoundedCornerShape(44.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(category.title.take(1), color = palette.accent,
                            fontWeight = FontWeight.Bold, fontSize = 34.sp)
                    }
                }
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = category.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    lineHeight = 27.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                )
            }
        }
    }
}

/** 3×3 mini collage of the category's first nine pictures (photos once attached, emoji until then). */
@Composable
private fun Montage(category: Category, palette: CategoryPalette, modifier: Modifier = Modifier) {
    val items = category.items.take(9)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { row ->
            Row(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                repeat(3) { col ->
                    val item = items.getOrNull(row * 3 + col)
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (item != null) {
                            ItemImage(
                                item = item,
                                accent = palette.banner,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(2.dp),
                                crop = category.previewOnTap,
                                glyphScale = 0.62f,
                            )
                        }
                    }
                }
            }
        }
    }
}
