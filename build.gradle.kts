// training-core — gemeinsamer Trainings-Kern fuer Fusion und Flowtimer
// (Flowtimer-Integration design.md Abschnitt 1). Pure Kotlin JVM, keine
// Android-Abhaengigkeiten. Beide Apps kompilieren diesen Quelltext als
// gewoehnliches Subprojekt mit der eigenen Toolchain; deshalb deklariert
// der Plugin-Block KEINE Version — die App-Settings liefert sie (Fusion
// 2.4.10, Flowtimer 2.4.0). Der Default hierzu steht in settings.gradle.kts.
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    id("org.jetbrains.kotlin.jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        // Konservative Sprachstufe (CONTEXT-Punkt 2, entschieden 2026-08-24):
        // Der Kern bleibt fuer beide Apps konsumierbar, selbst wenn eine
        // Seite auf aelterem Kotlin stehen bleibt.
        apiVersion.set(KotlinVersion.KOTLIN_2_0)
        languageVersion.set(KotlinVersion.KOTLIN_2_0)
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.truth:truth:1.4.4")
}
