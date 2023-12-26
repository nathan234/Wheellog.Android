import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    kotlin("multiplatform")
}

android {
    compileSdkVersion(34)

    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(34)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

//    kotlinOptions {
//        jvmTarget = JavaVersion.VERSION_17.toString()
//    }

    namespace = "com.wheellog.shared"
}

kotlin {
    android()
    ios()
    sourceSets {
        val commonMain by getting {
            dependencies {
            }
        }
        val commonTest by getting {
            dependencies {

            }
        }
        val androidMain by getting {
            dependencies {
                val coreKtxVersion = "1.12.0" // replace with your version
                val appcompactVersion = "1.6.1" // replace with your version
                val materialVersion = "1.10.0" // replace with your version
                implementation("androidx.core:core-ktx:$coreKtxVersion")
                implementation("androidx.appcompat:appcompat:$appcompactVersion")
                implementation("com.google.android.material:material:$materialVersion")
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
                implementation("com.google.truth:truth:1.1.5")
                implementation("org.junit.jupiter:junit-jupiter:5.10.0")
                implementation("io.mockk:mockk:1.13.8")
            }
        }
        val iosMain by getting
        val iosTest by getting
    }
}

//tasks.withType<KotlinCompile> {
//    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
//}
