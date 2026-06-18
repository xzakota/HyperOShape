plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.xzakota.oshape"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.xzakota.oshape"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }
    
    buildTypes.forEach {
        it.buildConfigField("String", "VERSION_NAME", "\"${defaultConfig.versionName}\"")
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
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
        buildConfig = true
    }
}

dependencies {
    // LSPosed API 101 - Core Xposed framework
    compileOnly("io.github.libxposed:api:101.0.0")
    
    // AndroidX
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    
    // DexKit for dynamic hooking
    implementation("org.luckypray:dexkit:2.0.5")
}
