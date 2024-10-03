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
        versionName = "2.3.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
    //noinspection GradleDependency
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-firestore:24.10.0")
    implementation ("com.google.android.gms:play-services-vision:20.1.3")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("androidx.activity:activity:1.8.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // CameraX core library using Camera2 implementation
    implementation("androidx.camera:camera-camera2:1.3.1")

// CameraX lifecycle library
    implementation ("androidx.camera:camera-lifecycle:1.3.1")

// CameraX view control library
    implementation ("androidx.camera:camera-view:1.3.1")

    implementation ("com.google.android.gms:play-services-location:21.0.1")

    implementation ("io.github.chaosleung:pinview:1.4.4")

    //TensorFlow Lite libraries (To recognize faces)
    implementation ("org.tensorflow:tensorflow-lite-task-vision:0.3.0")
    implementation  ("org.tensorflow:tensorflow-lite-support:0.3.0")
    implementation ("org.tensorflow:tensorflow-lite:0.0.0-nightly-SNAPSHOT")
    //ML Kit (To detect faces)
    implementation ("com.google.mlkit:face-detection:16.1.5")
    implementation ("com.google.android.gms:play-services-mlkit-face-detection:17.0.1")
    //GSON (Conversion of String to Map & Vice-Versa)
    implementation ("com.google.code.gson:gson:2.8.9")
}