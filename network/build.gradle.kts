import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(mycatalog.plugins.android.library)
    alias(mycatalog.plugins.kotlin.serialization)
    alias(mycatalog.plugins.kotlin.multiplatform)
    id("maven-publish")
}

apply(from = file("publish.gradle"))

kotlin {
    androidTarget {
        publishLibraryVariants("debug", "release")

        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }

        dependencies {
            debugImplementation(mycatalog.chucker.debug)
            debugImplementation(mycatalog.flipper)
            debugImplementation(mycatalog.flippernetworkplugin)
            debugImplementation(mycatalog.soLoader)
            releaseImplementation(mycatalog.chucker.release)
            releaseImplementation(mycatalog.flipper.noop)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        androidMain.dependencies {
            implementation(mycatalog.ktor.client.okhttp)
            implementation(mycatalog.certificate.transparency)
        }

        iosMain.dependencies {
            implementation(mycatalog.ktor.client.darwin)
        }

        commonMain.dependencies {
            implementation(mycatalog.ktor.client.core)
            implementation(mycatalog.ktor.client.content.negotiation)
            implementation(mycatalog.ktor.client.auth)
            implementation(mycatalog.ktor.client.logging)
            implementation(mycatalog.ktor.client.call.id)
            implementation(mycatalog.ktor.serialization.kotlinx.json)
            implementation(mycatalog.connectivity.device)
            implementation(mycatalog.ktor.client.mock)
        }

        commonTest.dependencies {
            implementation(mycatalog.kotlin.test)
        }

        // Required by KMM-ViewModel
        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
            languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
        }
    }
}


android {
    namespace = "com.tech.bazaar.kmp.network"
    compileSdk = 35
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
