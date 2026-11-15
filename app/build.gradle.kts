plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.sawti"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sawti"
        minSdk = 26
        targetSdk = 36
        versionCode = 14
        versionName = "1.6.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["appLabel"] = "@string/app_name"
    }

    // The production key is supplied only at release time by GitHub Actions secrets.
    // Local and PR builds continue to produce unsigned Release APKs for validation.
    signingConfigs {
        val storePath = System.getenv("SAWTI_KEYSTORE_PATH")
        if (!storePath.isNullOrBlank()) {
            create("production") {
                storeFile = file(storePath)
                storePassword = System.getenv("SAWTI_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("SAWTI_KEY_ALIAS")
                keyPassword = System.getenv("SAWTI_KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        debug {
            if (providers.gradleProperty("quranPreview").orNull == "true") {
                applicationIdSuffix = ".quranpreview"
                versionNameSuffix = "-quran-test"
                manifestPlaceholders["appLabel"] = "صوتي — اختبار القرآن"
            }
        }
        release {
            isMinifyEnabled = true
            // DrawableAssetResolver resolves optional image resources by name.
            // Keep resource shrinking off until all dynamic names are direct R references.
            isShrinkResources = false
            signingConfigs.findByName("production")?.let { signingConfig = it }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all {
            // Robolectric resolves Android runtime jars independently of Gradle repositories.
            // Use the canonical Maven Central endpoint instead of its repo1 alias.
            it.systemProperty("robolectric.dependency.repo.url", "https://repo.maven.apache.org/maven2")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Room: categories + word items (family, daily phrases, educational sets)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Loads attached photos from app storage
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.exifinterface:exifinterface:1.3.7")
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.16")
    testImplementation("androidx.compose.ui:ui-test-junit4")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}


// Checked-in schema history is the contract for non-destructive future upgrades.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}


