plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // 暂时注释掉 Hilt，先让项目能运行
    // alias(libs.plugins.ksp)
    // alias(libs.plugins.hilt)
}

android {
    namespace = "com.guyi.demo1"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.guyi.demo1"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")

    // Hilt - 暂时注释掉
    // implementation(libs.hilt.android)
    // ksp(libs.hilt.compiler)
    // implementation(libs.hilt.navigation.compose)

    // Network - 暂时注释掉
    // implementation(libs.retrofit)
    // implementation(libs.retrofit.converter.gson)
    // implementation(libs.okhttp)
    // implementation(libs.okhttp.logging)
    // implementation(libs.okhttp.eventsource)
    // implementation(libs.gson)

    // DataStore - 暂时注释掉
    // implementation(libs.datastore.preferences)

    // Navigation
    implementation(libs.navigation.compose)

    // Coil (Image Loading) - 暂时注释掉
    // implementation(libs.coil.compose)

    // Markdown - 暂时注释掉
    // implementation(libs.compose.markdown)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}