package com.sawti.presentation.settings

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import com.sawti.R
import com.sawti.domain.model.ThemeMode
import com.sawti.presentation.theme.SawtiTheme
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class AboutScreenTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var view: View

    private fun show(mode: ThemeMode, fontScale: Float = 1f) {
        compose.setContent {
            SawtiTheme(themeMode = mode, performanceMode = true) {
                view = LocalView.current
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) { AboutScreen() }
                }
            }
        }
    }

    private fun save(name: String) {
        compose.runOnIdle {
            val directory = File("build/outputs/about-review").apply { mkdirs() }
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            File(directory, "$name.png").outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            bitmap.recycle()
        }
    }

    @Test fun lightPageShowsTheDeveloperAndOpensAnEmailDraft() {
        show(ThemeMode.LIGHT)
        compose.onNodeWithText("إسماعيل بن سالم").assertIsDisplayed()
        save("about-light")
        compose.onNodeWithText("تواصل مع المطوّر").performScrollTo().performClick()
        val intent = shadowOf(RuntimeEnvironment.getApplication() as Application).nextStartedActivity
        assertEquals(Intent.ACTION_SENDTO, intent.action)
        assertEquals("mailto", intent.data?.scheme)
        assertEquals(DEVELOPER_EMAIL, intent.data?.schemeSpecificPart)
    }

    @Test fun darkPageKeepsDeveloperAndCopyrightReachable() {
        show(ThemeMode.DARK)
        compose.onNodeWithText("إسماعيل بن سالم").assertIsDisplayed()
        compose.onNodeWithText("© 2026 إسماعيل بن سالم\nجميع الحقوق محفوظة").performScrollTo().assertIsDisplayed()
        save("about-dark")
    }

    @Test fun twiceSizedTextKeepsEmailAndCopyrightReachable() {
        show(ThemeMode.LIGHT, fontScale = 2f)
        compose.onNodeWithText(DEVELOPER_EMAIL).performScrollTo().assertIsDisplayed()
        save("about-large-text")
        compose.onNodeWithText("© 2026 إسماعيل بن سالم\nجميع الحقوق محفوظة").performScrollTo().assertIsDisplayed()
    }

    @Test fun unavailableEmailAppShowsAUsefulMessage() {
        val context = object : ContextWrapper(RuntimeEnvironment.getApplication()) {
            override fun startActivity(intent: Intent) { throw ActivityNotFoundException() }
        }
        contactDeveloper(context)
        assertEquals(context.getString(R.string.about_no_email_app), ShadowToast.getTextOfLatestToast())
    }

    @Test fun largeSettingsChoicesWrapWithoutLosingSelection() {
        var selected = 0
        compose.setContent {
            SawtiTheme(performanceMode = true) {
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                    Column { ChoiceRow(listOf(0 to "عادي", 1 to "كبير", 2 to "كبير جدًا"), 0) { selected = it } }
                }
            }
        }
        compose.onNodeWithText("عادي").assertIsSelected()
        compose.onNodeWithText("كبير جدًا").assertIsDisplayed().performClick()
        assertEquals(2, selected)
    }
}
