# Persisted enum names are a database/SharedPreferences wire format.
# Keep only those enum definitions stable across R8-enabled releases.
-keep class com.sawti.domain.model.PracticeMode { *; }
-keep class com.sawti.domain.model.PracticeOutcome { *; }
-keep class com.sawti.domain.model.ThemeMode { *; }
-keep class com.sawti.domain.model.TextSize { *; }
-keep class com.sawti.domain.model.SpeechRate { *; }
-keep class com.sawti.domain.model.ArabicDialect { *; }
-keep class com.sawti.domain.model.TouchDwell { *; }

# Strip verbose/debug tracing from minified Release; retain warnings and errors.
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
}
