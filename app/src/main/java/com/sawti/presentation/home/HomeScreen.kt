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
import com.sawti.presentation.common.AdultAccentBar
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
    caregiverMode: Boolean,
    onOpen: (String) -> Unit,
    onQuickTap: (WordItem) -> Unit,
    onQuickLongTap: (WordItem) -> Unit,
) {
    if (categories.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    Column(Modifier.fillMaxSize()) {
        if (favorites.isNotEmpty() || caregiverMode) {
            FavoritesBar(favorites, activeItemId, caregiverMode, onQuickTap, onQuickLongTap)
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(categories, key = { it.id }) { category ->
                CategoryCard(category) { onOpen(category.id) }
            }
        }
    }
}

// ------------------------------------------------------------------ quick access

@Composable
private fun FavoritesBar(
    favorites: List<FavoriteEntry>,
    activeItemId: Long?,
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
                    FavoriteChip(entry, isActive = activeItemId == entry.item.id, onClick = { onTap(entry.item) }, onLongClick = { onLongTap(entry.item) })
                }
            }
        }
    }
}

/** One pinned card: tap = instant speech. */
@Composable
private fun FavoriteChip(entry: FavoriteEntry, isActive: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val palette = paletteOf(entry.palette)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = cardElevation(2.dp),
        border = if (isActive) BorderStroke(2.dp, palette.banner) else null,
        modifier = Modifier
            .width(116.dp)
            .heightIn(min = 132.dp)
            .bounceClick(onLongClick = onLongClick, onClick = onClick),
    ) {
        Column {
            AdultAccentBar(palette.banner)
            Box(
                Modifier
                    .height(82.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                ItemImage(
                    item = entry.item,
                    accent = palette.banner,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    crop = entry.item.categoryId == "family",
                    glyphScale = 0.62f,
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 6.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    entry.item.displayText,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
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
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 210.dp)
            .bounceClick(onClick = onClick),
    ) {
        Column {
            AdultAccentBar(palette.banner)
            Montage(category, palette, Modifier.fillMaxWidth().height(142.dp).padding(12.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = category.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
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
