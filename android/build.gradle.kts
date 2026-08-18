plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "com.leaf.osumania"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.leaf.osumania"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    sourceSets {
        named("main") {
            assets.srcDirs("../android/assets")
        }
    }
}

dependencies {
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx-backend-android:1.12.1")
    implementation("com.badlogicgames.gdx:gdx-platform:1.12.1:natives-armeabi-v7a")
    implementation("com.badlogicgames.gdx:gdx-platform:1.12.1:natives-arm64-v8a")
    implementation("com.badlogicgames.gdx:gdx-platform:1.12.1:natives-x86")
    implementation("com.badlogicgames.gdx:gdx-platform:1.12.1:natives-x86_64")
    implementation("com.badlogicgames.gdx:gdx-freetype-platform:1.12.1:natives-armeabi-v7a")
    implementation("com.badlogicgames.gdx:gdx-freetype-platform:1.12.1:natives-arm64-v8a")
    implementation("com.badlogicgames.gdx:gdx-freetype-platform:1.12.1:natives-x86")
    implementation("com.badlogicgames.gdx:gdx-freetype-platform:1.12.1:natives-x86_64")
    implementation("androidx.appcompat:appcompat:1.7.0")
}
