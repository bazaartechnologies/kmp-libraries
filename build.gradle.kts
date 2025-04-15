plugins {
    alias(mycatalog.plugins.android.library) apply false
    alias(mycatalog.plugins.android.application) apply false
    alias(mycatalog.plugins.kotlin.multiplatform) apply  false
    alias(mycatalog.plugins.compose.multiplatform) apply  false
    alias(mycatalog.plugins.compose.compiler) apply  false
    alias(mycatalog.plugins.kotlin.android) apply false
    alias(mycatalog.plugins.kotlin.serialization) apply false
    alias(mycatalog.plugins.native.couroutine) apply false
}

subprojects {
    group = "com.tech.bazaar.kmp"
    version = "3.0.0"
}
