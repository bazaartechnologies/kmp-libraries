import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.multiplatform)
    id("maven-publish")
    kotlin("native.cocoapods")
}

val enableIOSBuild = true
val flipperkit_version = "0.250.0"

apply(from = file("publish.gradle"))

kotlin {
    androidTarget {
        publishLibraryVariants("debug", "release")

        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    if (enableIOSBuild) {
        val xcFrameworkName = "Network"
        val xcf = XCFramework(xcFrameworkName)

        cocoapods {
            version = "1.0"
            summary = "Some description for a Kotlin/Native module"
            homepage = "Link to a Kotlin/Native module homepage"
            name = xcFrameworkName
            ios.deploymentTarget = "16.0"
            noPodspec()
            xcodeConfigurationToNativeBuildType["CUSTOM_DEBUG"] = NativeBuildType.DEBUG
            xcodeConfigurationToNativeBuildType["CUSTOM_RELEASE"] = NativeBuildType.RELEASE
//            pod(name = "FlipperKit") {
//                version = flipperkit_version
//            }
//
//            pod(name = "FlipperKit/FlipperKitLayoutComponentKitSupport") {
//                version = flipperkit_version
//            }
//
//            pod(name = "FlipperKit/SKIOSNetworkPlugin") {
//                version = flipperkit_version
//            }
//
//            pod(name = "FlipperKit/FlipperKitUserDefaultsPlugin") {
//                version = flipperkit_version
//            }
//
//            pod(name = "Flipper-DoubleConversion") { }
//
//            pod(name = "Flipper-Folly") { }
//
//            pod(name = "Flipper-Glog") { }
//
//            pod(name = "Flipper-PeerTalk") { }
//
//            pod(name = "CocoaLibEvent") { }
//
//            pod(name = "boost-for-react-native") { }
//
//            pod(name = "OpenSSL-Universal") { }
//
//            pod(name = "CocoaAsyncSocket") { }
//
//            pod(name = "ComponentKit") {
//                version = "0.31"
//            }

        }

        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = xcFrameworkName
                binaryOption("bundleId", "com.tech.bazaar.${xcFrameworkName.lowercase()}")
                isStatic = true
                @OptIn(ExperimentalKotlinGradlePluginApi::class)
                transitiveExport = true // This is default.
                xcf.add(this)
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.call.id)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.connectivity.device)
            implementation(libs.ktor.client.mock)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.certificate.transparency)
        }
        val androidRelease by creating {
            dependsOn(androidMain.get())
            dependencies {
                implementation(libs.flipper.noop)
                implementation(libs.chucker.release)
            }
        }
        val androidDebug by creating {
            dependsOn(androidMain.get())
            dependencies {
                implementation(libs.chucker.debug)
                implementation(libs.flipper)
                implementation(libs.flippernetworkplugin)
                implementation(libs.soLoader)
            }
        }

        val iosMain by creating {
            dependsOn(commonMain.get())
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        val iosX64Main by getting {
            dependsOn(iosMain)
        }
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
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
