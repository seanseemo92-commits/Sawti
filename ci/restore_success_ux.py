from pathlib import Path

p = Path("extracted/MyWords/app/src/main/java/com/mywords/presentation/settings/SettingsScreen.kt")
s = p.read_text(encoding="utf-8")
old = s
assert 'message = "تم الاسترجاع. سيُغلق التطبيق الآن؛ افتحه مجدداً."' in s
assert 'delay(1500)' in s
s = s.replace("import kotlinx.coroutines.delay\n", "")
s = s.replace(
    '    var pendingRestore by remember { mutableStateOf<android.net.Uri?>(null) }',
    '    var pendingRestore by remember { mutableStateOf<android.net.Uri?>(null) }\n    var restoreCompleted by remember { mutableStateOf(false) }'
)
s = s.replace(
    'بعد النجاح سيُعاد تشغيل التطبيق.',
    'بعد النجاح سيتطلب التطبيق إعادة فتحه لتحديث البيانات.'
)
s = s.replace(
    '''                            message = "تم الاسترجاع. سيُغلق التطبيق الآن؛ افتحه مجدداً."
                            delay(1500)
                            Process.killProcess(Process.myPid())''',
    '''                            // Show completion before terminating old Room connections.
                            restoreCompleted = true'''
)
needle = '''    Column(
        Modifier
            .fillMaxSize()'''
assert needle in s
s = s.replace(needle, '''    if (restoreCompleted) {
        AlertDialog(
            onDismissRequest = { /* Keep old Room handles inaccessible until exit. */ },
            title = { Text("تم الاسترجاع بنجاح") },
            text = { Text("تمت استعادة البيانات. يلزم إغلاق التطبيق وفتحه مجدداً لتحميل الكلمات والصور والتسجيلات المسترجعة بأمان.") },
            confirmButton = {
                TextButton(onClick = { Process.killProcess(Process.myPid()) }) {
                    Text("إغلاق التطبيق")
                }
            },
        )
    }
    Column(
        Modifier
            .fillMaxSize()''')
assert s != old
assert 'restoreCompleted = true' in s and 'delay(1500)' not in s
p.write_text(s, encoding="utf-8")
print("Restore success confirmation added")
