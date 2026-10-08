import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// ---------------------------------------------------------------------------
// Signierung fuer die Veroeffentlichung
//
// Der Schluessel liegt bewusst NICHT im Repo. Anlegen mit:
//
//   keytool -genkeypair -v -keystore handyhelfer-release.jks \
//           -alias handyhelfer -keyalg RSA -keysize 4096 -validity 10000
//
// Danach keystore.properties im Projektwurzelverzeichnis anlegen
// (steht in .gitignore):
//
//   storeFile=/absoluter/pfad/handyhelfer-release.jks
//   storePassword=...
//   keyAlias=handyhelfer
//   keyPassword=...
//
// Ohne diese Datei baut der Release-Zweig unsigniert weiter - praktisch zum
// Pruefen, aber nicht hochladbar.
// ---------------------------------------------------------------------------
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}
val hasReleaseKeystore = keystoreProperties.getProperty("storeFile") != null

android {
    namespace = "org.dialos.handyhelfer"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.dialos.handyhelfer"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    androidResources {
        // Nur Deutsch und Englisch mitnehmen. `resourceConfigurations` waere
        // der aeltere Weg und ist seit AGP 8.13 als ueberholt gemeldet.
        localeFilters += setOf("de", "en")
    }

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            if (hasReleaseKeystore) signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"

            // Eigener Name auf dem Startbildschirm. Ohne ihn heissen Test- und
            // Play-Fassung beide "HANDYHelfer" und liegen ununterscheidbar
            // nebeneinander - in DialOS Mobil hat genau das einmal einen halben
            // Geraetetest gekostet.
            resValue("string", "app_name", "HANDYHelfer (Test)")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

// ---------------------------------------------------------------------------
// Zu den Versionen: Lint meldet fuer alle vier eine neuere und liegt damit
// falsch. Am 2026-10-08 ausprobiert - core-ktx 1.19.1 verlangt
//
//   "requires libraries and applications that depend on it to compile
//    against version 37 or later of the Android APIs"
//
// waehrend AGP 8.13.0 als hoechste empfohlene compileSdk 36 nennt und API 37
// im SDK gar nicht liegt. Das Hochziehen einer einzelnen Zahl reisst also die
// ganze Werkzeugkette mit. Die Staende hier sind dieselben wie in DialOS
// Mobil und dort seit Monaten gruen.
//
// Wer das aendern will, aendert zuerst compileSdk und AGP - und dann alle vier
// zusammen, nicht einzeln.
// ---------------------------------------------------------------------------
dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("com.google.android.material:material:1.12.0")

    testImplementation("junit:junit:4.13.2")
}
