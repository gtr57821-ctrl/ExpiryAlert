
plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  id("com.google.gms.google-services")
}

android {
  namespace = "com.example.expiryalert"

  compileSdk {
    version = release(37)
  }

  defaultConfig {
    applicationId = "com.example.expiryalert"
    minSdk = 24
    targetSdk = 37
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      optimization {
        enable = false
      }
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  buildFeatures {
    compose = true
  }
}

dependencies {

  // FIREBASE
  implementation(platform("com.google.firebase:firebase-bom:34.19.0"))
  implementation("com.google.firebase:firebase-auth")
  implementation("com.google.firebase:firebase-firestore")

  // ESCÁNER DE CÓDIGOS DE BARRAS
  implementation("com.google.mlkit:barcode-scanning:17.3.0")

  // RECONOCIMIENTO DE TEXTO Y FECHAS
  implementation("com.google.mlkit:text-recognition:16.0.1")

  // JETPACK COMPOSE
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)

  // ANDROID
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)

  // PRUEBAS
  testImplementation(libs.junit)

  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)

  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
}
