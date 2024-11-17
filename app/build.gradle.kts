plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")

}

android {
    namespace = "com.watch.cypher"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.watch.cypher"
        minSdk = 24
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation ("com.google.android.material:material:1.10.0")
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.adapters)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.core.animation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(kotlin("script-runtime"))

    implementation ("com.github.lisawray.groupie:groupie:2.10.1")
    implementation ("com.github.lisawray.groupie:groupie-viewbinding:2.10.1")

    implementation ("com.squareup.okhttp3:okhttp:4.10.0")

    implementation ("org.java-websocket:Java-WebSocket:1.5.2")

    implementation ("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation ("androidx.room:room-ktx:2.6.1") // Optional - Kotlin extensions

    //Animation
    implementation (libs.lottie)
    implementation ("com.github.razir.progressbutton:progressbutton:2.1.0")

    //data
    implementation (libs.gson)

    //QR
    implementation ("com.google.zxing:core:3.4.1")   // For QR Code generation
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")


    //Glide
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation ("com.github.bumptech.glide:okhttp3-integration:4.15.1")


    implementation ("com.kroegerama:bottomsheet-imagepicker:1.1.2")

    implementation ("com.github.AsynctaskCoffee:VoiceRecorder:beta-0.5")


    implementation ("com.github.massoudss:waveformSeekBar:5.0.2")
    implementation ("com.github.lincollincol:amplituda:2.2.2")


}