// Top-level build file
plugins {
    id("com.android.application") version "9.2.0" apply false
    // AGP 9 bringt eingebaute Kotlin-Unterstützung mit – das separate
    // org.jetbrains.kotlin.android-Plugin wird NICHT mehr benötigt.
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.0" apply false
    id("com.google.devtools.ksp") version "2.3.9" apply false
}
