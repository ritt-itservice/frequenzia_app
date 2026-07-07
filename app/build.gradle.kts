import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    // Kein "org.jetbrains.kotlin.android" mehr nötig – AGP 9 hat
    // eingebaute Kotlin-Unterstützung. Nur der Compose-Compiler-Plugin
    // wird noch separat benötigt.
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

// Lädt lokale Signing-Zugangsdaten aus keystore.properties (nicht in Git).
// Falls die Datei fehlt (z. B. bei anderen Entwicklern/CI ohne Release-Key),
// wird einfach kein Release-Signing konfiguriert.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
val hasKeystoreProperties = keystorePropertiesFile.exists()
if (hasKeystoreProperties) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "de.rittitservice.frequenzia"
    compileSdk = 37

    defaultConfig {
        applicationId = "de.rittitservice.frequenzia"
        minSdk = 26
        targetSdk = 37
        versionCode = 5
        versionName = "1.3.0"
    }

    signingConfigs {
        if (hasKeystoreProperties) {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hasKeystoreProperties) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    sourceSets {
        // Room-Schema-Historie (siehe ksp { arg("room.schemaLocation", ...) }
        // unten) als Debug-Asset verfügbar machen: Robolectric liest für
        // JVM-Unit-Tests die Assets des Debug-Varianten-Merges (nicht die
        // des "test"-Source-Sets), siehe generateDebugUnitTestConfig.
        getByName("debug").assets.srcDirs("$projectDir/schemas")
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

// Kotlin-Zielversion für die Bytecode-Erzeugung (Android-Bytecode bleibt
// bei JVM 17, unabhängig davon, mit welcher JDK-Version Gradle selbst läuft)
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    // Core / Compose
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation(platform("androidx.compose:compose-bom:2026.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Media3 / ExoPlayer für Streaming + Background Playback
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    // HLS-Streams (.m3u8) sind bei Radiosendern verbreitet (u.a. RTL); ohne
    // diese Erweiterung bricht ExoPlayer beim Laden mit einer
    // ClassNotFoundException ab, unbemerkt tief in der MediaSession-Ebene.
    implementation("androidx.media3:media3-exoplayer-hls:1.4.1")
    implementation("androidx.media3:media3-session:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")

    // Netzwerk (Radio Browser API)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Bilder laden (Sender-Favicons)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Lokale Favoriten
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    debugImplementation("androidx.compose.ui:ui-tooling")

    // Tests (JVM-Unit-Tests via Robolectric, kein Emulator nötig)
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.room:room-testing:2.8.4")
    testImplementation("androidx.sqlite:sqlite-framework:2.6.2")
    testImplementation("org.robolectric:robolectric:4.13")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}
