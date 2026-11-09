package com.sawti.presentation.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sawti.R
import com.sawti.domain.model.Category
import com.sawti.presentation.common.CardImageContent
import com.sawti.presentation.common.categoryDrawableName
import com.sawti.presentation.common.drawableResourceId
import com.sawti.presentation.common.StaggeredCascade
import com.sawti.presentation.common.bounceClick
import com.sawti.presentation.theme.LocalIsDark
import com.sawti.presentation.theme.LocalPerformanceMode
import com.sawti.presentation.theme.paletteOf
import com.sawti.presentation.theme.categoryBannerColor

private val CardRadius = RoundedCornerShape(20.dp)
private val BannerRadius = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 20.dp, bottomEnd = 20.dp)

@Composable
fun HomeScreen(categories: List<Category>, speakingItemId: Long?, onOpen: (String) -> Unit,
    onOpenQuran: () -> Unit,
    onRetry: () -> Unit = {}, isLoading: Boolean = true, loadError: String? = null, retryEnabled: Boolean = true) {
    if (categories.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
            contentAlignment = Alignment.Center) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (isLoading && loadError == null) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Text(text = "جارٍ تحميل الأقسام...", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                } else {
                    Text(text = loadError ?: "لا توجد أقسام متاحة حالياً.", style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center)
                    Button(onClick = onRetry, enabled = retryEnabled && !isLoading) { Text("إعادة المحاولة") }
                }
            }
        }
        return
    }
    val columns = if (LocalConfiguration.current.screenWidthDp >= 600) 3 else 2
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyVerticalGrid(columns = GridCells.Fixed(columns), modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            item(key = "quran-feature", span = { GridItemSpan(2) }) {
                StaggeredCascade(row = 0, modifier = Modifier.fillMaxWidth()) {
                    QuranHeroCard(onClick = onOpenQuran)
                }
            }
            itemsIndexed(categories, key = { _, category -> category.id }) { index, category ->
                StaggeredCascade(row = (index + 2) / columns, modifier = Modifier.fillMaxWidth()) {
                    LuxuryCategoryCard(category = category,
                        isSpeaking = speakingItemId != null && category.items.any { it.id == speakingItemId },
                        onClick = { onOpen(category.id) })
                }
            }
        }
    }
}


@Composable
private fun QuranHeroCard(onClick: () -> Unit) {
    val dark = LocalIsDark.current
    val performance = LocalPerformanceMode.current
    val green = if (dark) Color(0xFF103A31) else Color(0xFF175A47)
    val gold = Color(0xFFE8CE91)

    // Uses the same rounded tile, elevation and bounce/haptic behavior as the
    // surrounding home categories. Text remains real Compose text for accessibility.
    Surface(
        modifier = Modifier.fillMaxWidth().clip(CardRadius)
            .bounceClick(onClick = onClick, accent = gold),
        shape = CardRadius,
        color = green,
        border = BorderStroke(1.dp, gold.copy(alpha = if (dark) 0.28f else 0.35f)),
        shadowElevation = if (performance) 0.dp else if (dark) 4.dp else 8.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(168.dp).background(green),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // In RTL the text stays on the right; photo occupies 62% of the card.
            Column(
                modifier = Modifier.weight(0.38f).padding(start = 14.dp, end = 2.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.MenuBook,
                    contentDescription = null,
                    tint = gold,
                    modifier = Modifier.size(26.dp),
                )
                Text(
                    text = "القرآن الكريم",
                    color = Color.White,
                    fontSize = 23.sp,
                    lineHeight = 29.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                )
                Text(
                    text = "سور قصيرة للتلاوة والتكرار",
                    color = Color(0xFFF1E4C7),
                    fontSize = 12.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 3,
                )
            }

            // Keep the existing local Quran artwork. A narrow fade softens only the image edge.
            Box(modifier = Modifier.weight(0.62f).fillMaxHeight()) {
                Image(
                    painter = painterResource(R.drawable.img_quran_hero),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.horizontalGradient(
                            0f to Color.Transparent,
                            0.82f to Color.Transparent,
                            1f to green.copy(alpha = 0.95f),
                        )
                    )
                )
            }
        }
    }
}

@Composable
private fun LuxuryCategoryCard(category: Category, isSpeaking: Boolean, onClick: () -> Unit) {
    val dark = LocalIsDark.current
    val performance = LocalPerformanceMode.current
    val palette = paletteOf(category.palette)
    val bannerColor = categoryBannerColor(category.id, palette.banner)
    val resId = drawableResourceId(categoryDrawableName(category.id))
    val surfaceColor = if (dark) Color(0xFF131B26) else bannerColor
    val borderColor = if (dark) Color.White.copy(alpha = 0.09f) else Color(0xFF0F172A).copy(alpha = 0.07f)
    // Create a running transition only when the aura is actually rendered.
    val auraGlow = if (isSpeaking && !performance) {
        val transition = rememberInfiniteTransition(label = "VoiceAuraTransition")
        val glow by transition.animateFloat(initialValue = 0.35f, targetValue = 0.85f,
            animationSpec = infiniteRepeatable(animation = tween(900, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
            label = "AuraPulse")
        glow
    } else 0f
    Box(modifier = Modifier.fillMaxWidth().aspectRatio(0.85f).then(
        if (isSpeaking && !performance) Modifier.drawBehind {
            drawCircle(brush = Brush.radialGradient(
                colors = listOf(bannerColor.copy(alpha = auraGlow * 0.39f), bannerColor.copy(alpha = 0.12f), Color.Transparent),
                center = center, radius = size.maxDimension * 0.72f))
        } else Modifier).clip(CardRadius).bounceClick(onClick = onClick, accent = bannerColor)) {
        Surface(modifier = Modifier.fillMaxSize(), shape = CardRadius, color = surfaceColor,
            border = BorderStroke(width = if (isSpeaking) 2.dp else 1.dp, color = if (isSpeaking) bannerColor else borderColor),
            shadowElevation = if (performance) 0.dp else if (dark) 4.dp else 8.dp) {
            Box(modifier = Modifier.fillMaxSize()) {
                val heroItem = category.items.firstOrNull { it.photoPath != null } ?: category.items.firstOrNull()
                if (resId != null) {
                    Image(painter = painterResource(resId), contentDescription = category.title,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else if (heroItem != null) {
                    CardImageContent(item = heroItem, accent = palette.banner, isCurrentlyPlaying = isSpeaking,
                        modifier = Modifier.fillMaxSize(), crop = true, fullBleed = true)
                }
                Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().clip(BannerRadius)
                    .background(bannerColor).padding(horizontal = 8.dp, vertical = 5.dp), contentAlignment = Alignment.Center) {
                    Text(text = category.title, color = Color.White, fontSize = 18.sp, lineHeight = 24.sp,
                        fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
