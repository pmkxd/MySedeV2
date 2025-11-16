plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.test.mysede"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.test.mysede"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    // Desugar para compatibilidad con APIs modernas en versiones antiguas de Android
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // AndroidX Core
    implementation(libs.appcompat)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Material Design (usar solo una versión)
    implementation("com.google.android.material:material:1.13.0")

    // Navigation Components
    implementation(libs.navigation.runtime)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // ========== FIREBASE (usando BoM para versiones compatibles) ==========
    implementation(platform("com.google.firebase:firebase-bom:33.7.0")) // Última versión estable
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-firestore") // Para guardar notificaciones
    implementation("com.google.firebase:firebase-messaging") // Para FCM (notificaciones push)

    // ========== WORK MANAGER (Para notificaciones programadas) ==========
    implementation("androidx.work:work-runtime:2.9.0")

    // ========== SWIPE REFRESH LAYOUT (Para refrescar notificaciones) ==========
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // ========== CARDVIEW (Para los items de notificación) ==========
    implementation("androidx.cardview:cardview:1.0.0")

    // ========== RECYCLERVIEW (Si no está incluido en tus libs) ==========
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Subida de archivos a Cloudinary
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Manejo de imágenes de perfil
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.github.yalantis:ucrop:2.2.8")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}