plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.sutra.app"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    flavorDimensions += "sutra"
    productFlavors {
        create("xinjing") {
            dimension = "sutra"
            applicationId = "com.sutra.xinjing"
            resValue("string", "app_name", "心经诵读")
        }
        create("jingangjing") {
            dimension = "sutra"
            applicationId = "com.sutra.jingangjing"
            resValue("string", "app_name", "金刚经诵读")
        }
        create("dizangjing") {
            dimension = "sutra"
            applicationId = "com.sutra.dizangjing"
            resValue("string", "app_name", "地藏经诵读")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.webkit:webkit:1.8.0")
    implementation("androidx.core:core-ktx:1.12.0")
}