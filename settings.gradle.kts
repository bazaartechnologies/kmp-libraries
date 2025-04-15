pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("mycatalog") {
            from(files("/Users/abbashussain/AndroidStudioProjects/kmp-libraries/gradle/mycatalog.versions.toml"))
        }
    }
}

rootProject.name = "kmp-libraries"
include(":network")
include(":app")
