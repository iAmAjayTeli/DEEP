import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.deep.eyedonation"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.deep.eyedonation"
        minSdk = 26
        targetSdk = 36
        versionCode = 4
        versionName = "1.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())

        val apiKey = properties.getProperty("MAPS_API") ?: ""
        val placesWebKey = properties.getProperty("PLACES_WEB_API_KEY") ?: ""

        // Keep this if you're using BuildConfig elsewhere
        buildConfigField("String", "MAPS_API", "\"$apiKey\"")
        buildConfigField("String", "PLACES_WEB_API_KEY", "\"$placesWebKey\"")

        // 🔴 This is needed for the Manifest!
        manifestPlaceholders["MAPS_API_KEY"] = apiKey
        manifestPlaceholders["PLACES_WEB_API_KEY"] = placesWebKey

        // Add logging for release build
        buildConfigField("boolean", "LOG_ENABLED", "true")

    }


    packagingOptions {
        resources {
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/LICENSE"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )

            ndk {
                debugSymbolLevel = "FULL"
            }

        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        dataBinding = true
    }

    buildFeatures {
        buildConfig = true
    }

    firebaseCrashlytics{
        nativeSymbolUploadEnabled = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //firebase
    implementation(libs.firebase.firestore)

    //lottie animation
    implementation(libs.lottie)

    //google maps and places
    implementation(libs.play.services.maps)
    implementation(libs.places)

    //retrofit and gson
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // Import the BoM for the Firebase platform
    implementation(platform(libs.firebase.bom))

    // Add the dependencies for the Crashlytics NDK and Analytics libraries
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation(libs.firebase.crashlytics.ndk)
    implementation(libs.firebase.analytics)




}