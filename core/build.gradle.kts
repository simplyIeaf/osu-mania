plugins {
    id("android-library")
    id("kotlin-android")
}

android {
    namespace = "com.leaf.osumania.core"
    compileSdk = 35
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    api("com.badlogicgames.gdx:gdx:1.12.1")
    api("com.badlogicgames.gdx:gdx-freetype:1.12.1")
}
