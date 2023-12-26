import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdkVersion(34)

    defaultConfig {
        applicationId = "com.cooper.wheellog"
        minSdkVersion(24)
        targetSdkVersion(34)
        versionCode = 103
        versionName = "1.0.8b"
        multiDexEnabled = true
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("../debug.keystore")
            // You can also set storePassword, keyAlias, keyPassword, etc.
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    lintOptions {
        isAbortOnError = false
    }

    namespace = "com.cooper.wheellog"
}

val kotlinVersion = "1.9.0" // replace with your actual Kotlin version
val coreKtxVersion = "1.12.0" // replace with your actual Core KTX version
val appcompactVersion = "1.6.1" // replace with your actual AppCompat version
val materialVersion = "1.10.0" // replace with your actual Material version

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("androidx.core:core-ktx:$coreKtxVersion")
    implementation("androidx.appcompat:appcompat:$appcompactVersion")
    implementation("com.google.android.material:material:$materialVersion")
    implementation("com.google.android.support:wearable:2.9.0")
    implementation("com.google.android.gms:play-services-wearable:18.1.0")
    implementation("androidx.wear:wear:1.3.0")
    implementation(project(":shared"))
    testImplementation("junit:junit:4.13.2")
    compileOnly("com.google.android.wearable:wearable:2.9.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}

//tasks.withType<KotlinCompile> {
//    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
//}
