plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    compileSdk = 34

    defaultConfig {
        applicationId = project.group.toString()

        minSdk = 21
        targetSdk = 34

        versionCode = 30
        versionName = project.version.toString()

        multiDexEnabled = true
    }

    splits {
        abi {
            reset()
            isUniversalApk = true
        }
    }

    signingConfigs {
        create("ci") {
            storeFile = System.getenv("ANDROID_NIGHTLY_KEYSTORE")?.let { file(it) }
            storePassword = System.getenv("ANDROID_NIGHTLY_KEYSTORE_PASSWORD")
            keyAlias = System.getenv("ANDROID_NIGHTLY_KEYSTORE_ALIAS")
            keyPassword = System.getenv("ANDROID_NIGHTLY_KEYSTORE_PASSWORD")
        }
    }

    namespace = project.group.toString()

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            manifestPlaceholders["appName"] = "Muza (Debug)"
        }

        release {
            isMinifyEnabled = false
            isShrinkResources = false
            manifestPlaceholders["appName"] = "Muza"
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "RELEASE_HACK", "\"AndroidWhyTfDidYouMakeMeDoThis\"")
        }

        create("X") {
            initWith(getByName("release"))
            matchingFallbacks += "release"

            applicationIdSuffix = ".x"
            versionNameSuffix = "-X"
            manifestPlaceholders["appName"] = "Muza X"
            signingConfig = signingConfigs.findByName("ci")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + listOf("-Xcontext-receivers")
    }

    packaging {
        resources.excludes.add("META-INF/**/*")
    }
}

kotlin {
    jvmToolchain(libs.versions.jvm.get().toInt())
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(projects.compose.persist)
    implementation(projects.compose.preferences)
    implementation(projects.compose.routing)
    implementation(projects.compose.reordering)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.activity)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.util)
    implementation(libs.compose.ripple)
    implementation(libs.compose.shimmer)
    implementation(libs.compose.coil)
    implementation(libs.compose.material3)

    implementation(libs.palette)

    implementation(libs.exoplayer)
    implementation(libs.exoplayer.workmanager)
    implementation(libs.workmanager)
    implementation(libs.workmanager.ktx)

    implementation(libs.kotlin.coroutines)
    implementation(libs.kotlin.immutable)

    implementation(libs.room)
    ksp(libs.room.compiler)

    implementation(projects.providers.github)
    implementation(projects.providers.innertube)
    implementation(projects.providers.kugou)
    implementation(projects.providers.lrclib)
    implementation(projects.providers.piped)
    implementation(projects.core.data)
    implementation(projects.core.ui)

    coreLibraryDesugaring(libs.desugaring)

    detektPlugins(libs.detekt.compose)
    detektPlugins(libs.detekt.formatting)

    implementation ("com.yandex.android:mobileads:6.4.0")
    implementation("com.google.android.gms:play-services-ads-identifier:18.0.1")
}
