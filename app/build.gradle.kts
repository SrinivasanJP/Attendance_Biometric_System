plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "dev.roxs.attendance"
    compileSdk = 34

    defaultConfig {
        applicationId = "dev.roxs.attendance"
        minSdk = 26
        //noinspection EditedTargetSdkVersion
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
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-firestore:24.10.0")
    implementation ("com.google.android.gms:play-services-vision:20.1.3")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // CameraX core library using Camera2 implementation
    implementation("androidx.camera:camera-camera2:1.3.0")

// CameraX lifecycle library
    implementation ("androidx.camera:camera-lifecycle:1.3.0")

// CameraX view control library
    implementation ("androidx.camera:camera-view:1.3.0")
}