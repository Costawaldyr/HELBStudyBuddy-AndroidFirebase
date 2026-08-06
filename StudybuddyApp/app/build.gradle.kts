import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

// Load properties from local.properties
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) {
        load(FileInputStream(f))
    }
}

val claudeApiKey: String = (localProps.getProperty("CLAUDE_API_KEY")
    ?: System.getenv("CLAUDE_API_KEY")
    ?: "")

val mapboxAccessToken: String = (localProps.getProperty("MAPBOX_ACCESS_TOKEN")
    ?: System.getenv("MAPBOX_ACCESS_TOKEN")
    ?: "")

android {
    namespace = "com.example.studybuddy"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.studybuddy"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Exposed to Java code as BuildConfig.CLAUDE_API_KEY.
        buildConfigField("String", "CLAUDE_API_KEY", "\"$claudeApiKey\"")
        
        // Inject Mapbox Access Token as a string resource
        resValue("string", "mapbox_access_token", mapboxAccessToken)
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
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

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.cardview)

    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.ai)
    implementation(libs.firebase.analytics)

    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.play.services.auth)

    implementation(libs.play.services.location)
    implementation(libs.mapbox)

    implementation(libs.room.runtime)
    implementation(libs.work.runtime)
    annotationProcessor(libs.room.compiler)

    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.common.java8)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)

    implementation(libs.circleimageview)
    implementation(libs.glide)
    implementation(libs.flexbox)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
