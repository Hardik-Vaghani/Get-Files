plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.hardik.getfiles"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.hardik.getfiles"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Kotlin Coroutines Core
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3") // Check for the latest version

    // Kotlin Coroutines Android
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3") // Check for the latest version

    // ViewModel and LiveData (if not already included)
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6") // Check for the latest version
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6") // Check for the latest version

}