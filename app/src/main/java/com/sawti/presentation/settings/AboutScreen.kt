package com.sawti.presentation.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.sawti.R
import com.sawti.presentation.theme.CardShape
import com.sawti.presentation.theme.cardBorder
import com.sawti.presentation.theme.cardElevation

internal const val DEVELOPER_EMAIL = "ismail92sa@gmail.com"

/** Only a user tap opens the email app; no analytics or automatic messages. */
internal fun contactDeveloper(context: Context) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", DEVELOPER_EMAIL, null))
        .putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.about_email_subject))
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, R.string.about_no_email_app, Toast.LENGTH_LONG).show()
    } catch (_: SecurityException) {
        Toast.makeText(context, R.string.about_no_email_app, Toast.LENGTH_LONG).show()
    }
}

@Composable
fun AboutScreen() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val version = remember(context) {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = 600.dp).fillMaxWidth()
                .verticalScroll(rememberScrollState()).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // ImageView supports the actual adaptive launcher icon in both themes.
            AndroidView(
                factory = { viewContext -> ImageView(viewContext).apply {
                    setImageResource(R.mipmap.ic_launcher)
                    importantForAccessibility = android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO
                } },
                modifier = Modifier.size(88.dp),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.about_app_name), style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                Text("Sawti", style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.about_tagline), textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.about_version, version),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(shape = CardShape, color = MaterialTheme.colorScheme.surface,
                border = cardBorder(), shadowElevation = cardElevation(2.dp)) {
                Column(Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.about_developed_by),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.about_developer_name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }
            OutlinedButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                contactDeveloper(context)
            }, shape = CardShape, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.Email, contentDescription = null)
                    Text(stringResource(R.string.about_contact), fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center)
                    Text(DEVELOPER_EMAIL, style = MaterialTheme.typography.bodyMedium.copy(
                        textDirection = TextDirection.Ltr), textAlign = TextAlign.Center)
                }
            }
            Text(stringResource(R.string.about_contact_hint),
                style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(stringResource(R.string.about_copyright),
                style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp))
        }
    }
}
