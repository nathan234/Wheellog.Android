// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val kotlinVersion = "1.9.0"
    val composeVersion = "1.5.3"
    val lifecycleVersion = "2.6.2"
    val roomVersion = "2.5.2"
    val materialVersion = "1.10.0"
    val appcompactVersion = "1.6.1"
    val coreKtxVersion = "1.12.0"

    repositories {
        mavenCentral()
        google()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.1.2")
        classpath("com.neenbedankt.gradle.plugins:android-apt:1.8")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }

    extra["kotlinVersion"] = kotlinVersion
    extra["composeVersion"] = composeVersion
    extra["lifecycleVersion"] = lifecycleVersion
    extra["roomVersion"] = roomVersion
    extra["materialVersion"] = materialVersion
    extra["appcompactVersion"] = appcompactVersion
    extra["coreKtxVersion"] = coreKtxVersion
}

allprojects {
    repositories {
        mavenCentral()
        maven(url = "https://jitpack.io")
        google()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
