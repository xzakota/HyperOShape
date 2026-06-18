plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.xzakota.oshape"
    compileSdk = 34

    defaultConfig {
        minSdk = 30
        targetSdk = 34

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xno-param-assertions")
    }
    
    buildFeatures {
        aidl = true
    }
}

dependencies {
    // LSPosed API 101 - Core Xposed framework
    compileOnly("io.github.libxposed:api:101")
    
    // AndroidX
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    
    // DexKit for dynamic hooking
    implementation("org.lsposed.dexkit:dexkit:2.0.4")
}
