package com.sawti.presentation.category

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import com.sawti.R
import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import com.sawti.domain.model.ArabicDialect
import com.sawti.domain.model.Category
import com.sawti.domain.model.DialectCatalog
import com.sawti.domain.model.ThemeMode
import com.sawti.presentation.home.HomeScreen
import com.sawti.presentation.theme.GreetingsCoral
import com.sawti.presentation.theme.SawtiTheme
import java.io.File
import java.lang.reflect.Proxy
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class GreetingsLayoutTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var view: View
    private val tapped = mutableListOf<Long>()
    private val spoken = mutableListOf<Long>()
    private val actions = Proxy.newProxyInstance(LibraryActions::class.java.classLoader,
        arrayOf(LibraryActions::class.java)) { proxy, method, args ->
        when (method.name) {
            "onItemTapped" -> { tapped += (args!![0] as com.sawti.domain.model.WordItem).id; Unit }
            "onSpeakAgain" -> { spoken += (args!![0] as com.sawti.domain.model.WordItem).id; Unit }
            "hashCode" -> System.identityHashCode(proxy)
            "equals" -> proxy === args?.firstOrNull()
            "toString" -> "Greetings test actions"
            else -> Unit
        }
    } as LibraryActions

    private fun category(dialect: ArabicDialect = ArabicDialect.STANDARD): Category {
        val items = WordSeed.itemEntities().filter { it.categoryId == "greetings" }
            .mapIndexed { index, entity ->
                DialectCatalog.localize(entity.toDomain().copy(id = index + 1L), dialect)
            }
        return Category("greetings", "التحيات", 5, false, items)
    }

    private fun show(mode: ThemeMode, fontScale: Float = 1f, content: @Composable () -> Unit) {
        compose.setContent {
            SawtiTheme(themeMode = mode, performanceMode = true) {
                view = LocalView.current
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        Box(Modifier.fillMaxSize()) { content() }
                    }
                }
            }
        }
    }

    private fun save(name: String) {
        compose.runOnIdle {
            val directory = File("build/outputs/greetings-review").apply { mkdirs() }
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            File(directory, "$name.png").outputStream().use {
                assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
            bitmap.recycle()
        }
    }

    @Test fun homeCoverUsesTheSameCardGeometryAndNavigationAsFamily() {
        val greeting = category()
        val family = Category("family", "عائلتي", 0, true, emptyList())
        var opened = ""
        show(ThemeMode.LIGHT) {
            HomeScreen(listOf(greeting, family), null, { opened = it }, {}, isLoading = false)
        }
        val greetingBounds = compose.onNodeWithText("التحيات").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val familyBounds = compose.onNodeWithText("عائلتي").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertEquals(familyBounds.width, greetingBounds.width, 0.1f)
        assertEquals(familyBounds.height, greetingBounds.height, 0.1f)
        save("greetings-home-light")
        compose.onNodeWithText("التحيات").performClick()
        assertEquals("greetings", opened)
    }

    @Test fun lightGridKeepsAllFourteenCardsAndRepeatedTaps() {
        val category = category()
        show(ThemeMode.LIGHT) {
            CategoryScreen(category, LibraryUiState(categories = listOf(category), isLoadingCategories = false), actions)
        }
        save("greetings-grid-light")
        val grid = compose.onNodeWithTag("word-grid")
        for ((index, item) in category.items.withIndex()) {
            grid.performScrollToIndex(index)
            compose.onNodeWithTag("word-card-${item.id}").assertIsDisplayed().performClick()
        }
        compose.onNodeWithTag("word-card-${category.items.last().id}").performClick()
        assertEquals(category.items.map { it.id } + category.items.last().id, tapped)
    }

    @Test @Config(qualifiers = "w320dp-h800dp")
    fun expandedArabicWordCaptionIsNeverLimitedToFourLines() {
        val original = category()
        val phrase = "أُرِيدُ الذَّهَابَ إِلَى الْمُسْتَشْفَى وَأَحْتَاجُ إِلَى الْمُسَاعَدَةِ وَالْمُرَافَقَةِ فِي الطَّرِيقِ"
        val changed = original.copy(items = original.items.mapIndexed { index, word ->
            if (index == 0) word.copy(displayText = phrase) else word
        })
        show(ThemeMode.LIGHT, fontScale = 1.5f) {
            CategoryScreen(changed, LibraryUiState(categories = listOf(changed), isLoadingCategories = false), actions)
        }
        val results = mutableListOf<TextLayoutResult>()
        compose.onNodeWithText(phrase, useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { results.addAll(it) }
        val layout = results.single()
        assertTrue("Caption should naturally wrap on 320dp", layout.lineCount > 4)
        assertFalse((0 until layout.lineCount).any(layout::isLineEllipsized))
    }

    @Test fun darkLibyanGridPreservesItsLabelsAndSharedStyle() {
        val category = category(ArabicDialect.LIBYAN)
        show(ThemeMode.DARK) {
            CategoryScreen(category, LibraryUiState(categories = listOf(category), isLoadingCategories = false), actions)
        }
        compose.onNodeWithTag("word-card-${category.items.first().id}").assertIsDisplayed()
        save("greetings-grid-dark")
    }

    private fun assertFirstTwoCardsShareARow(category: Category) {
        val first = compose.onNodeWithTag("word-card-${category.items[0].id}")
            .assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val second = compose.onNodeWithTag("word-card-${category.items[1].id}")
            .assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertEquals("The first two cards must share a row", first.top, second.top, 1f)
        assertEquals(first.width, second.width, 1f)
        assertTrue("Cards must not overlap", first.right <= second.left || second.right <= first.left)
    }

    @Test fun compactPhoneGridShowsTwoCardsPerRow() {
        val category = category(ArabicDialect.LIBYAN)
        show(ThemeMode.LIGHT) {
            CategoryScreen(category, LibraryUiState(categories = listOf(category), isLoadingCategories = false), actions)
        }
        assertFirstTwoCardsShareARow(category)
        save("greetings-grid-compact-two-columns")
    }

    @Test
    @Config(qualifiers = "w320dp-h720dp")
    fun narrowerPhoneKeepsTwoColumnsWithTwiceSizedArabicLabels() {
        val category = category()
        show(ThemeMode.DARK, fontScale = 2f) {
            CategoryScreen(category, LibraryUiState(categories = listOf(category), isLoadingCategories = false), actions)
        }
        assertFirstTwoCardsShareARow(category)
        compose.onNodeWithTag("word-grid").performScrollToIndex(category.items.lastIndex)
        compose.onNodeWithTag("word-card-${category.items.last().id}").assertIsDisplayed().performClick()
        assertEquals(listOf(category.items.last().id), tapped)
        save("greetings-grid-narrow-large-font")
    }

    @Test fun twiceSizedArabicLabelsAndLastCardRemainReachable() {
        val category = category()
        show(ThemeMode.LIGHT, fontScale = 2f) {
            CategoryScreen(category, LibraryUiState(categories = listOf(category), isLoadingCategories = false), actions)
        }
        compose.onNodeWithTag("word-card-${category.items.first().id}").assertIsDisplayed()
        save("greetings-grid-large")
        compose.onNodeWithTag("word-grid").performScrollToIndex(category.items.lastIndex)
        compose.onNodeWithTag("word-card-${category.items.last().id}").assertIsDisplayed().performClick()
        assertEquals(listOf(category.items.last().id), tapped)
    }

    @Test fun flashcardModeRetainsTheSamePhotoCaptionAndListenControl() {
        val category = category()
        show(ThemeMode.LIGHT) {
            CategoryScreen(category, LibraryUiState(categories = listOf(category), isLoadingCategories = false), actions)
        }
        val context = RuntimeEnvironment.getApplication()
        compose.onNodeWithContentDescription(context.getString(R.string.view_flashcards)).performClick()
        compose.onNodeWithText(category.items.first().displayText).assertIsDisplayed()
        compose.onNodeWithTag("flashcard-listen-${category.items.first().id}").assertIsDisplayed().performClick()
        assertEquals(listOf(category.items.first().id), spoken)
        save("greetings-flashcard")
    }

    @Test fun focusDialogRetainsTheGreetingsPaletteInsteadOfTheOldPurplePreset() {
        val category = category()
        val item = category.items.first()
        show(ThemeMode.LIGHT) {
            CategoryScreen(category, LibraryUiState(categories = listOf(category),
                focusItemId = item.id, isLoadingCategories = false), actions)
        }
        val layout = mutableListOf<TextLayoutResult>()
        compose.onNode(hasText(item.displayText) and hasAnyAncestor(isDialog()))
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layout) }
        assertEquals(GreetingsCoral, layout.single().layoutInput.style.color)
    }

    @Test @Config(sdk = [36], qualifiers = "w320dp-h720dp")
    fun android16CompactCategoryAtDoubleFont() {
        val c = category()
        show(ThemeMode.DARK, fontScale = 2f) {
            CategoryScreen(c, LibraryUiState(categories = listOf(c), isLoadingCategories = false), actions)
        }
        assertFirstTwoCardsShareARow(c)
        compose.onNodeWithTag("word-grid").performScrollToIndex(c.items.lastIndex)
        compose.onNodeWithTag("word-card-${c.items.last().id}").assertIsDisplayed()
    }

    @Test @Config(sdk = [36], qualifiers = "w600dp-h840dp")
    fun android16TabletCategoryShowsMultipleColumns() {
        val c = category()
        show(ThemeMode.LIGHT, fontScale = 2f) {
            CategoryScreen(c, LibraryUiState(categories = listOf(c), isLoadingCategories = false), actions)
        }
        assertFirstTwoCardsShareARow(c)
    }

}
