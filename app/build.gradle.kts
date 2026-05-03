import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// 1. Try to read from local.properties first (Private/Local)
var weatherApiKey = ""
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    val props = Properties()
    localPropertiesFile.inputStream().use { props.load(it) }
    weatherApiKey = props.getProperty("WEATHER_API_KEY") ?: ""
}

// 2. Fallback to gradle.properties if local.properties didn't have a valid key
if (weatherApiKey.isEmpty() || weatherApiKey == "YOUR_API_KEY_HERE") {
    val projectKey = (project.findProperty("WEATHER_API_KEY") ?: "").toString()
    if (projectKey.isNotEmpty() && projectKey != "YOUR_API_KEY_HERE") {
        weatherApiKey = projectKey
    }
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "WEATHER_API_KEY", "\"$weatherApiKey\"")
    }

    @Suppress("UnstableApiUsage")
    androidResources {
        localeFilters += "en"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            enableUnitTestCoverage = false
            enableAndroidTestCoverage = false
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }
}

val finalApiKey = weatherApiKey
tasks.register("printApiKey") {
    val keyToPrint = finalApiKey
    doLast {
        if (keyToPrint.isEmpty() || keyToPrint == "YOUR_API_KEY_HERE") {
            println("WARNING: No valid API key found. Please add WEATHER_API_KEY to local.properties")
        } else {
            println("DEBUG_API_KEY_START:[$keyToPrint]:DEBUG_API_KEY_END")
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.volley)
    implementation(libs.cardview)
    implementation(libs.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
