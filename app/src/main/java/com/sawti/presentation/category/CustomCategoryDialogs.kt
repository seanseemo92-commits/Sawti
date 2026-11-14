package com.sawti.presentation.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.sawti.domain.model.Category
import com.sawti.presentation.theme.paletteOf

/** Built-in categories have no management actions. Only the "custom_" namespace is editable. */
@Composable
fun CustomCategoryManager(
    categories: List<Category>,
    onCreate: (String, Int, Int) -> Unit,
    onUpdate: (String, String, Int, Int) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val custom = categories.filter { it.id.startsWith("custom_") }
    var selected by remember { mutableStateOf<String?>(null) }
    var title by remember(selected) { mutableStateOf(custom.firstOrNull { it.id == selected }?.title.orEmpty()) }
    var palette by remember(selected) { mutableStateOf(custom.firstOrNull { it.id == selected }?.palette ?: 2) }
    var sort by remember(selected) { mutableStateOf(((custom.firstOrNull { it.id == selected }?.let { index -> categories.indexOf(index) }) ?: categories.size).coerceAtLeast(0).toString()) }
    var pendingDelete by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إدارة الأقسام المخصصة") },
        text = {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("الأقسام الأساسية محمية من التعديل والحذف.")
                custom.forEach { c ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(onClick = { selected = c.id }, modifier = Modifier.weight(1f)) {
                            Text("تعديل: ${c.title}")
                        }
                        TextButton(
                            onClick = { pendingDelete = c.id },
                            enabled = c.items.isEmpty(),
                        ) { Text("حذف") }
                    }
                    if (c.items.isNotEmpty()) {
                        Text("انقل ${c.items.size} بطاقة من «${c.title}» قبل حذفه.", style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (selected != null) TextButton(onClick = { selected = null }) { Text("قسم جديد +") }
                OutlinedTextField(
                    value = title, onValueChange = { title = it.take(70) },
                    label = { Text("اسم القسم") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                val theme = paletteOf(palette)
                Surface(color = theme.pastel, modifier = Modifier.fillMaxWidth()) {
                    Text("اللون المختار: ${palette + 1}", color = theme.accent, modifier = Modifier.padding(12.dp))
                }
                Slider(value = palette.toFloat(), onValueChange = { palette = it.toInt().coerceIn(0,11) },
                    valueRange = 0f..11f, steps = 10)
                OutlinedTextField(
                    value = sort, onValueChange = { sort = it.filter(Char::isDigit).take(5) },
                    label = { Text("الترتيب (0–10000)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(enabled = title.isNotBlank() && (sort.toIntOrNull() ?: -1) in 0..10_000, onClick = {
                val order = sort.toIntOrNull() ?: return@Button
                val id = selected
                if (id == null) onCreate(title.trim(), palette, order)
                else onUpdate(id, title.trim(), palette, order)
                onDismiss()
            }) { Text(if (selected == null) "إنشاء القسم" else "حفظ التعديل") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إغلاق") } },
    )
    val deleting = custom.firstOrNull { it.id == pendingDelete }
    if (deleting != null) AlertDialog(
        onDismissRequest = { pendingDelete = null },
        title = { Text("تأكيد حذف القسم") },
        text = { Text("حذف القسم الفارغ «${deleting.title}»؟ لا يمكن التراجع عن الحذف.") },
        confirmButton = {
            Button(onClick = { onDelete(deleting.id); pendingDelete = null }) { Text("حذف القسم") }
        },
        dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("إلغاء") } },
    )
}

/** Create a new word only in a caregiver-created category; built-in words are unchanged. */
@Composable
fun AddCustomWordDialog(
    categoryTitle: String,
    onAdd: (String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf("") }
    var spoken by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة بطاقة إلى ${categoryTitle}") },
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = text, onValueChange = { text = it.take(100) },
                    label = { Text("نص البطاقة") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = spoken, onValueChange = { spoken = it.take(250) },
                    label = { Text("النص المنطوق (اختياري)") }, modifier = Modifier.fillMaxWidth())
                Text("تقدر تضيف صورة أو تسجيل صوتي بعد إنشاء البطاقة.")
            }
        },
        confirmButton = {
            Button(enabled = text.isNotBlank(), onClick = {
                onAdd(text.trim(), spoken.trim())
                onDismiss()
            }) { Text("إضافة البطاقة") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } },
    )
}
