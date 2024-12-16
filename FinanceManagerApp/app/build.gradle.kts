plugins {
    alias(libs.plugins.android.application)

    // from the firebase set up
//    id("com.android.application")
    // google service gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.financemanagerapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.financemanagerapp"
        minSdk = 23
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
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase dependencies with latest BoM
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")

    // GraphView library (no exclusions needed unless identified as problematic)
    implementation("com.jjoe64:graphview:4.2.2")

    // Exclude support-compat from conflicting libraries
    implementation(libs.firebase.firestore) {
        exclude(group = "com.android.support", module = "support-compat")
    }

    // for the donut graph in goal tracker.
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation(libs.preference)

    //for GSON
    implementation("com.google.code.gson:gson:2.8.9")


    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}


