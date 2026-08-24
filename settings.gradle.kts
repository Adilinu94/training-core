// training-core — Standalone-Build. Als Subprojekt in Fusion/Flowtimer
// eingebunden (include mit projectDir) bleibt dieses Settings-File ohne
// Wirkung; es versorgt nur die eigene CI mit einer Plugin-Default-Version.
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    plugins {
        id("org.jetbrains.kotlin.jvm") version "2.4.10"
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "training-core"
