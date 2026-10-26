package com.sawti.presentation.common

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.sawti.R
import com.sawti.presentation.theme.RecordRed

/**
 * Record / stop / play / delete controls, shared by the family editor and the dashboard tile editor.
 * Asks for the microphone permission the first time.
 */
@Composable
fun VoiceRecordingSection(
    hasRecording: Boolean,
    isRecording: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onPlay: () -> Unit,
    onDelete: () -> Unit,
    onPickFile: (Uri) -> Unit = {},
) {
    val context = LocalContext.current
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) onStart()
        else Toast.makeText(context, R.string.mic_permission_needed, Toast.LENGTH_LONG).show()
    }

    // existing audio file from the phone; the ViewModel copies it into filesDir/custom_audio/
    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onPickFile(uri)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (isRecording) {
            Button(
                onClick = onStop,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RecordRed),
            ) {
                Icon(Icons.Filled.Stop, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.stop_recording), fontSize = 18.sp)
            }
        } else {
            Button(
                onClick = {
                    val granted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (granted) onStart() else micPermission.launch(Manifest.permission.RECORD_AUDIO)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
            ) {
                Icon(Icons.Filled.Mic, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.record_voice), fontSize = 18.sp)
            }
        }

        if (!isRecording) {
            OutlinedButton(
                onClick = { audioPicker.launch("audio/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
            ) {
                Icon(Icons.Filled.AudioFile, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.pick_audio_file), fontSize = 16.sp)
            }
        }

        if (hasRecording && !isRecording) {
            Text(stringResource(R.string.voice_saved), fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onPlay, modifier = Modifier.heightIn(min = 48.dp)) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Text(stringResource(R.string.play_recording))
                }
                OutlinedButton(onClick = onDelete, modifier = Modifier.heightIn(min = 48.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Text(stringResource(R.string.delete_recording))
                }
            }
        }
    }
}
