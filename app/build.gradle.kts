plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") // ✅ Make sure this is present
    id("com.google.gms.google-services") // ✅ Firebase
}


android {
    namespace = "com.pant.girly"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.pant.girly"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    android {
        buildFeatures {
            viewBinding = true  // ✅ Correct Kotlin DSL syntax
        }
    }

    dependencies {
        implementation("androidx.core:core-ktx:1.12.0")
        implementation("androidx.appcompat:appcompat:1.6.1")
        implementation("com.google.android.material:material:1.10.0")
        implementation("androidx.constraintlayout:constraintlayout:2.1.4")
        implementation("androidx.appcompat:appcompat:1.6.1")

        // Firebase Dependencies
        implementation(platform("com.google.firebase:firebase-bom:32.7.3"))
        implementation("com.google.firebase:firebase-auth-ktx")
        implementation("com.google.firebase:firebase-firestore-ktx")
        implementation("com.google.firebase:firebase-storage-ktx")
        implementation("com.google.firebase:firebase-auth-ktx")
        testImplementation("junit:junit:4.13.2")
        androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
        implementation ("com.google.firebase:firebase-auth:22.3.1")
        implementation ("com.google.firebase:firebase-firestore:24.10.1")
        implementation ("com.google.firebase:firebase-database:20.2.2")
        implementation ("com.github.bumptech.glide:glide:4.15.1")
        kapt ("com.github.bumptech.glide:compiler:4.15.1")
        implementation ("com.google.firebase:firebase-auth:22.3.1")
        implementation ("com.google.firebase:firebase-appcheck-playintegrity:17.0.0")
        implementation ("com.google.firebase:firebase-auth-ktx:22.2.0")
        implementation ("com.google.firebase:firebase-appcheck-playintegrity:16.0.0")
        implementation ("de.hdodenhof:circleimageview:3.1.0")
        implementation ("com.google.android.gms:play-services-maps:18.2.0")
        implementation ("com.google.android.gms:play-services-location:21.0.1")
        implementation ("androidx.camera:camera-camera2:1.2.0")
        implementation ("androidx.camera:camera-lifecycle:1.2.0")
        implementation ("androidx.camera:camera-view:1.2.0")
        implementation("com.google.firebase:firebase-storage-ktx:20.3.0")
        implementation ("com.google.android.material:material:1.6.0")



    }

    android {
        compileOptions {
            sourceCompatibility(JavaVersion.VERSION_1_8)
            targetCompatibility(JavaVersion.VERSION_1_8)
        }

        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}
apply(plugin = "com.google.gms.google-services")
dependencies {
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.preference)
    implementation(libs.material)
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
}

fun kapt(s: String) {

}
