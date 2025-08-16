plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.google.services)
}

android {
    buildFeatures {
        buildConfig = true
    }
    namespace = "com.example.reportviolation"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.reportviolation"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Room schema location
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true"
                )
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    flavorDimensions += listOf("env")

    productFlavors {
        create("lan") {
            dimension = "env"
            buildConfigField("String", "BASE_URL", "\"http://192.168.29.250:3000\"")
        }
        create("emulator") {
            dimension = "env"
            buildConfigField("String", "BASE_URL", "\"http://192.168.29.250:3000\"")
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material:1.6.1")
    
    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    
    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)
    
    // Google Maps and Location
    implementation(libs.google.maps)
    implementation(libs.google.location)
    
    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    
    // Dependency Injection - Temporarily removed Hilt
    // implementation(libs.hilt.android)
    // kapt(libs.hilt.compiler)
    // implementation(libs.androidx.hilt.navigation.compose)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.coroutines.core)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    
    // DataStore
    implementation(libs.androidx.datastore.preferences)
    
    // Work Manager
    implementation(libs.androidx.work.ktx)
    
    // Biometric
    implementation(libs.androidx.biometric)
    
    // Image Loading
    implementation(libs.coil.compose)
    
    // ML Kit
    implementation(libs.mlkit.text.recognition)

    // ... other dependencies
    implementation("androidx.compose.material:material-icons-extended-android:1.7.8")
    
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.core)
    testImplementation(libs.androidx.arch.core.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // (removed duplicate explicit networking deps; using versions from libs.versions.toml)

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")

    // EncryptedSharedPreferences
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
}