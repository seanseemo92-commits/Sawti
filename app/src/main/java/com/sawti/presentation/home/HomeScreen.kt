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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
private val BannerRadius = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 16.dp,
    bottomStart = 20.dp,
    bottomEnd = 20.dp,
)

@Composable
fun HomeScreen(
    categories: List<Category>,
    speakingItemId: Long?,
    onOpen: (String) -> Unit,
    onRetry: () -> Unit = {},
) {
    if (categories.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Text(
                    text = "جارٍ تحميل الأقسام...",
                    modifier = Modifier.padding(top = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        return
    }

    val columns = if (LocalConfiguration.current.screenWidthDp >= 600) 3 else 2

    // شاشة نقية، خالية من التشتيت، وموجهة بالكامل للأقسام الكبيرة
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            itemsIndexed(categories, key = { _, category -> category.id }) { index, category ->
                StaggeredCascade(
                    row = index / columns,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LuxuryCategoryCard(
                        category = category,
                        isSpeaking = speakingItemId != null && category.items.any { it.id == speakingItemId },
                        onClick = { onOpen(category.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LuxuryCategoryCard(
    category: Category,
    isSpeaking: Boolean,
    onClick: () -> Unit,
) {
    val dark = LocalIsDark.current
    val performance = LocalPerformanceMode.current
    val palette = paletteOf(category.palette)
    val bannerColor = categoryBannerColor(category.id, palette.banner)
    // An optional packaged image is used when WebP files are added to res/drawable.
    val resId = drawableResourceId(categoryDrawableName(category.id))

    // The photographic stage touches the card edges; a soft border follows the outer shape.
    val surfaceColor = if (dark) Color(0xFF131B26) else bannerColor
    val borderColor = if (dark) Color.White.copy(alpha = 0.09f) else Color(0xFF0F172A).copy(alpha = 0.07f)

    // أنيميشن هالة الصوت المشعة الحية (Bioluminescent Aura)
    val infiniteTransition = rememberInfiniteTransition(label = "VoiceAuraTransition")
    val auraGlow by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f) // نسبة هندسية ذهبية ممتلئة وشامخة
            .then(
                if (isSpeaking && !performance) {
                    Modifier.drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    bannerColor.copy(alpha = auraGlow * 0.39f),
                                    bannerColor.copy(alpha = 0.12f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = size.maxDimension * 0.72f
                            )
                        )
                    }
                } else Modifier
            )
            .clip(CardRadius)
            .bounceClick(onClick = onClick, accent = bannerColor)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = CardRadius,
            color = surfaceColor,
            border = BorderStroke(
                width = if (isSpeaking) 2.dp else 1.dp,
                color = if (isSpeaking) bannerColor else borderColor
            ),
            shadowElevation = if (performance) 0.dp else if (dark) 4.dp else 8.dp,
        ) {
            // Keep the photo edge-to-edge behind a content-sized, rounded
            // title banner. This removes the old 30%-height empty text area.
            Box(modifier = Modifier.fillMaxSize()) {
                val heroItem = category.items.firstOrNull { it.photoPath != null }
                    ?: category.items.firstOrNull()

                if (resId != null) {
                    Image(
                        painter = painterResource(resId),
                        contentDescription = category.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else if (heroItem != null) {
                    CardImageContent(
                        item = heroItem,
                        accent = palette.banner,
                        isCurrentlyPlaying = isSpeaking,
                        modifier = Modifier.fillMaxSize(),
                        crop = true,
                        fullBleed = true,
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .clip(BannerRadius)
                        .background(bannerColor)
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = category.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
