import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

android {
    compileSdkVersion(34)

    signingConfigs {
        getByName("debug") {
            storeFile = file("../debug.keystore")
            // You can also set storePassword, keyAlias, keyPassword, etc.
        }
    }

    defaultConfig {
        applicationId = "com.cooper.wheellog"
        minSdkVersion(21)
        targetSdkVersion(34)
        versionCode = 121
        versionName = "3.1.2b"

        buildConfigField("String", "BUILD_TIME", "\"${SimpleDateFormat("HH:mm dd.MM.yyyy").format(
            Date()
        )}\"")
        buildConfigField("String", "BUILD_DATE", "\"${SimpleDateFormat("dd.MM.yyyy").format(Date())}\"")

        vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true

        javaCompileOptions {
            annotationProcessorOptions {
                arguments(mapOf("room.schemaLocation" to "$projectDir/schemas",
                        "room.incremental" to "true",
                        "room.expandProjection" to "true"))
            }
        }

        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    buildTypes.all {
        val props = Properties()
        val propsFile = file("../local.properties")
        if (propsFile.exists()) {
            props.load(propsFile.inputStream())
        }

        val ecToken = System.getenv("ec_accessToken") ?: props.getProperty("ec_accessToken", "")
        buildConfigField("String", "ec_accessToken", "\"$ecToken\"")

        val metricaApi = System.getenv("metrica_api") ?: props.getProperty("metrica_api", "")
        buildConfigField("String", "metrica_api", "\"$metricaApi\"")
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.2"
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_17)
        targetCompatibility(JavaVersion.VERSION_17)
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.apply {
            isIncludeAndroidResources = true
            all {
                it.jvmArgs("-Xmx2g")
            }
        }
    }

    packagingOptions {
        jniLibs {
            excludes.add("META-INF/LICENSE*")
            excludes.add("META-INF/NOTICE*")
        }
        resources {
            excludes.add("META-INF/DEPENDENCIES")
            excludes.add("META-INF/LICENSE*")
            excludes.add("META-INF/NOTICE*")
            excludes.add("META-INF/ASL2.0")
            excludes.add("META-INF/*.kotlin_module")
        }
    }

    lintOptions {
        isAbortOnError = false
        disable("ComposableNaming")
    }

    namespace = "com.cooper.wheellog"
}

val composeVersion by extra("1.5.3")
val lifecycleVersion by extra("2.6.2")
val roomVersion by extra("2.5.2")
val coreKtxVersion by extra("1.12.0")
val appcompactVersion by extra("1.6.1")
val materialVersion by extra("1.10.0")
val kotlinVersion by extra("1.9.0")

dependencies {
    implementation(project(":shared"))

    // Compose
    implementation("androidx.compose.foundation:foundation:$composeVersion")
    implementation("androidx.compose.runtime:runtime:$composeVersion")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling:$composeVersion")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.navigation:navigation-compose:2.7.4")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")

    // Database
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    // Bluetooth
    implementation("com.github.weliem:blessed-android:2.4.1")

    // Samsung and Garmin libs
    implementation(fileTree("libs") { include("*.jar") })
    implementation("com.garmin.connectiq:ciq-companion-app-sdk:2.0.3@aar")

    // Pebble
    implementation("com.getpebble:pebblekit:4.0.1")

    // WearOS
    implementation("com.google.android.gms:play-services-wearable:18.1.0")
    wearApp(project(":wearos"))

    // Common
    implementation("androidx.core:core-ktx:$coreKtxVersion")
    implementation("androidx.appcompat:appcompat:$appcompactVersion")
    implementation("com.google.android.material:material:$materialVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("me.relex:circleindicator:2.1.6")
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("org.osmdroid:osmdroid-android:6.1.17")
    implementation("org.nanohttpd:nanohttpd:2.3.1")
    implementation("com.google.guava:guava:32.1.2-jre")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.truth:truth:1.1.5")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.robolectric:robolectric:4.10.3")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.0")
    testImplementation("org.json:json:20230618")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
