import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    //
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.cesoft.cesrunner"
    compileSdk = rootProject.extra.get("compileSdkVer") as Int

    defaultConfig {
        applicationId = "com.cesoft.cesrunner"
        minSdk = rootProject.extra.get("minSdkVer") as Int
        targetSdk = rootProject.extra.get("compileSdkVer") as Int
        versionCode = 1
        versionName = "0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        android.buildFeatures.buildConfig = true
        val keystoreFile = project.rootProject.file("local.properties")
        val properties = Properties()
        properties.load(keystoreFile.inputStream())
        var key = properties.getProperty("GEMINI_KEY") ?: ""
        buildConfigField(type = "String", name = "GEMINI_KEY", value = key)
        key = properties.getProperty("OPENAI_KEY") ?: ""
        buildConfigField(type = "String", name = "OPENAI_KEY", value = key)
        key = properties.getProperty("OPENROUTER_KEY") ?: ""
        buildConfigField(type = "String", name = "OPENROUTER_KEY", value = key)
        key = properties.getProperty("DEEPSEEK_KEY") ?: ""
        buildConfigField(type = "String", name = "DEEPSEEK_KEY", value = key)
        key = properties.getProperty("OLLAMA_KEY") ?: ""
        buildConfigField(type = "String", name = "OLLAMA_KEY", value = key)
        key = properties.getProperty("GROQ_KEY") ?: ""
        buildConfigField(type = "String", name = "GROOQ_KEY", value = key)
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        //compileOptions { jvmTarget = JvmTarget.JVM_24.target }
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            //resources.excludes.add("META-INF/NOTICE.md")
            //resources.excludes.add("META-INF/LICENSE.md")
            resources.excludes.add("META-INF/INDEX.LIST")
            resources.excludes.add("META-INF/DEPENDENCIES")
            resources.excludes.add("META-INF/io.netty.versions.properties")
        }
    }
}

dependencies {
    implementation(project(":data"))
    implementation(project(":domain"))

    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.compose.material.icons.extended)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // DI - Koin
    implementation(libs.koinCore)
    implementation(libs.koinAndroid)
    implementation(libs.koinAnnotations)
    implementation(libs.koinCompose)
    ksp(libs.koinKspCompiler)

    // MIV - Adidas
    implementation(libs.mvi)
    implementation(libs.mvi.compose)

    // MAPS - Open Street Maps
    implementation(libs.osmdroid.android)

    // AI Agents
    implementation(libs.koog.agents)
    // JSON
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
}