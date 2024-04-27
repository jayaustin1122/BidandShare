plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.bidnshare"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.bidnshare"
        minSdk = 24
        targetSdk = 33
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
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.firebase:firebase-messaging-ktx:23.3.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    val nav_version = "2.7.2"
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation (platform("com.google.firebase:firebase-bom:31.2.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))
    implementation ("androidx.fragment:fragment-ktx:1.6.1")
    // Kotlin
    //noinspection GradleDependency
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    //noinspection GradleDependency
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
    implementation ("com.github.shuhart:stepview:1.5.1")
    //pereetyy time
    implementation ("org.ocpsoft.prettytime:prettytime:5.0.1.Final")

    //circular
    implementation("com.mikhaellopez:circularprogressbar:3.1.0")
    implementation ("pl.droidsonroids.gif:android-gif-drawable:1.2.27")
    // dependency for circular ImageView
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    //karumi to access other component of mobile
    implementation ("com.karumi:dexter:6.2.2")

    //glide
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.14.2")


    //coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    //Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.6.2")
    implementation ("com.squareup.retrofit2:converter-gson:2.6.0")

    implementation ("pub.devrel:easypermissions:3.0.0")


    implementation( "androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    implementation ("com.sun.mail:javax.mail:1.6.2")

    implementation("androidx.biometric:biometric:1.1.0")
    implementation ("com.squareup.picasso:picasso:2.8")
}