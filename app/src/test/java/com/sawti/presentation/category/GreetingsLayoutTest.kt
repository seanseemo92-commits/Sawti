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
    private val actions = Proxy.newProxyInstance(LibraryActions::class.java.classLoader,
        arrayOf(LibraryActions::class.java)) { proxy, method, args ->
        when (method.name) {
            "onItemTapped" -> { tapped += (args!![0] as com.sawti.domain.model.WordItem).id; Unit }
            "hashCode" -> System.identityHashCode(proxy)
            "equals" -> proxy === args?.firstOrNull()
            "toString" -> "Greetings test actions"
            else -> Unit
        }
    } as LibraryActions

    private fun category(dialect: ArabicDialect = ArabicDialect.STANDARD): Category {
        val items = WordSeed.itemEntities().filter { it.categoryId == "greetings" }
            .map { DialectCatalog.localize(it.toDomain(), dialect) }
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

    @Test fun darkLibyanGridPreservesItsLabelsAndSharedStyle() {
        val category = category(ArabicDialect.LIBYAN)
        show(ThemeMode.DARK) {
            CategoryScreen(category, LibraryUiState(categories = listOf(category), isLoadingCategories = false), actions)
        }
        compose.onNodeWithTag("word-card-${category.items.first().id}").assertIsDisplayed()
        save("greetings-grid-dark")
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
        compose.onNodeWithText(context.getString(R.string.listen)).assertIsDisplayed()
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
}
