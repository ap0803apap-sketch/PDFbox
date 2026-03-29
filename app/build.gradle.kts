plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.ap.pdf.box"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ap.pdf.box"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0(29-03-26)"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = false
        checkReleaseBuilds = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.glide)

    // ✅ ROOM (KSP — FIXED)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // PDF
    implementation(libs.pdfbox.android)
    implementation(libs.android.pdf.viewer)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
