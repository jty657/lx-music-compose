plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.lxmusic.mobile"
    compileSdk {
        version = release(37) {
            minorApiLevel = 0
        }
    }

    defaultConfig {
        applicationId = "com.lxmusic.mobile"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures { compose = true }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    // Compose Multiplatform 1.11.1（Miuix 0.9.3 的构建版本；Android 上解析为 AndroidX Compose 1.11.2）
    implementation("org.jetbrains.compose.runtime:runtime:1.11.1")
    implementation("org.jetbrains.compose.foundation:foundation:1.11.1")
    implementation("org.jetbrains.compose.ui:ui:1.11.1")
    implementation("org.jetbrains.compose.material3:material3:1.11.1")
    implementation("org.jetbrains.compose.animation:animation:1.11.1")

    // Miuix 0.9.3 — HyperOS 风格卡片 / 偏好项 / 图标 / Navigation3 / 触感反馈
    implementation("top.yukonga.miuix.kmp:miuix-ui:0.9.3")
    implementation("top.yukonga.miuix.kmp:miuix-preference:0.9.3")
    implementation("top.yukonga.miuix.kmp:miuix-icons:0.9.3")
    implementation("top.yukonga.miuix.kmp:miuix-blur:0.9.3")

    // Liquid Glass / Backdrop 2.0.1（可调节背景模糊层、开屏与弹窗表面；API 31 以下自动降级）
    implementation("io.github.kyant0:backdrop:2.0.1")

    // Lifecycle / ViewModel
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.activity:activity-compose:1.12.2")
    implementation("androidx.core:core-ktx:1.17.0")

    // 数据层
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // 网络与序列化
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // QuickJS 引擎（原版同款：wang.harlon.quickjs:wrapper-android:2.4.0）
    implementation("wang.harlon.quickjs:wrapper-android:2.4.0")
    implementation("wang.harlon.quickjs:wrapper-java:2.4.0")

    // 播放器
    implementation("androidx.media3:media3-exoplayer:1.8.0")
    implementation("androidx.media3:media3-session:1.8.0")
}
