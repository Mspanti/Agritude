plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.pant.agritude"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pant.agritude"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
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

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation("androidx.room:room-runtime:2.6.1")
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.androidx.navigation.compose.android)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.benchmark.common)
    implementation(libs.common)
    implementation(libs.generativeai)
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    val lifecycle_version = "2.8.0"
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycle_version")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.04.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended:1.6.8")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.04.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("com.patrykandpatryk.vico:compose:1.6.0")
    implementation("com.patrykandpatryk.vico:compose-m3:1.14.0")

    // **TensorFlow Lite Dependencies**
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")

    // Update these two lines to 2.19.0
    implementation("org.tensorflow:tensorflow-lite:2.15.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.15.0")


    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.activity:activity-ktx:1.8.2")

    // TensorFlow Lite dependencies for the Financial Model
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.2")
    implementation("org.tensorflow:tensorflow-lite-task-text:0.4.2")
    implementation("org.tensorflow:tensorflow-lite-task-audio:0.4.2")
    implementation("com.google.android.gms:play-services-tflite-java:16.0.0")
    implementation("com.google.mediapipe:tasks-genai:0.10.24")
    implementation("com.google.android.gms:play-services-tflite-java:16.0.0")

    // Corrected versions for MediaPipe LLM dependencies
    implementation("com.google.mediapipe:tasks-vision:0.10.2")
    implementation ("com.google.mediapipe:tasks-genai:0.10.14")
    implementation("io.coil-kt:coil-compose:2.6.0")
    
    implementation("com.google.code.gson:gson:2.10")



}