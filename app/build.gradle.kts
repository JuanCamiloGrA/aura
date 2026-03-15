import org.gradle.testing.jacoco.tasks.JacocoReport
import java.util.Properties

val geminiApiKey = providers.gradleProperty("GEMINI_API_KEY").orElse("").get()

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

fun propertyOrEnv(name: String): String =
    providers.gradleProperty(name).orElse(System.getenv(name) ?: localProperties.getProperty(name) ?: "").get()

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    jacoco
}

val coverageExclusions = listOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Test*.*",
    "android/**/*.*",
    "**/*_Factory.class",
    "**/*_Factory$*.class",
    "**/*_MembersInjector.class",
    "**/*Module_*Factory.class",
    "**/*Dao_Impl.class",
    "**/*Dao_Impl$*.class",
    "**/*Database_Impl.class",
    "**/*Database_Impl$*.class",
    "**/*_Impl.class",
    "**/*_Impl$*.class",
    "**/*\$\$serializer.class",
    "**/*\$\$serializer$*.class",
    "**/*Preview*.*",
    "**/ComposableSingletons$*.*",
    "**/AndroidSpeechRecognizer.class",
    "**/AndroidSpeechRecognizer$*.class",
    "**/AndroidSpeechRecognizerKt.class",
    "**/AndroidSpeechRecognizerKt$*.class",
    "**/RecognitionClient.class",
    "**/RecognitionClient$*.class",
    "**/AndroidRecognitionClient.class",
    "**/AndroidRecognitionClient$*.class",
    "**/AndroidTextToSpeechEngine.class",
    "**/AndroidTextToSpeechEngine$*.class",
    "**/TextToSpeechSpeaker.class",
    "**/TextToSpeechSpeaker$*.class",
    "**/AndroidPlatformTextToSpeechSpeaker.class",
    "**/AndroidPlatformTextToSpeechSpeaker$*.class",
    "**/core/di/*.class",
    "**/core/di/*$*.class",
    "**/AndroidWallpaperController.class",
    "**/AndroidWallpaperController$*.class",
    "**/*SectionKt.class",
    "**/*SectionKt$*.class",
    "**/*ButtonKt.class",
    "**/*ButtonKt$*.class",
)

val kotlinDebugTree = fileTree("$buildDir/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes") {
    exclude(coverageExclusions)
}

val javaDebugTree = fileTree("$buildDir/intermediates/javac/debug/compileDebugJavaWithJavac/classes") {
    exclude(coverageExclusions)
}

val mainSrc = "$projectDir/src/main/java"

android {
    val releaseStoreFile = propertyOrEnv("AURA_RELEASE_STORE_FILE")
    val releaseStorePassword = propertyOrEnv("AURA_RELEASE_STORE_PASSWORD")
    val releaseKeyAlias = propertyOrEnv("AURA_RELEASE_KEY_ALIAS")
    val releaseKeyPassword = propertyOrEnv("AURA_RELEASE_KEY_PASSWORD")
    val hasReleaseSigning =
        releaseStoreFile.isNotBlank() &&
            releaseStorePassword.isNotBlank() &&
            releaseKeyAlias.isNotBlank() &&
            releaseKeyPassword.isNotBlank()

    namespace = "com.humans.aura"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.humans.aura"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseStoreFile)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
                enableV4Signing = true
            }
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            isJniDebuggable = false
            isCrunchPngs = true
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }

    bundle {
        abi {
            enableSplit = true
        }
        density {
            enableSplit = true
        }
        language {
            enableSplit = false
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/DEPENDENCIES",
                "/META-INF/LICENSE*",
                "/META-INF/NOTICE*",
            )
        }
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    flavorDimensions += "deviceTier"

    productFlavors {
        create("samsung") {
            dimension = "deviceTier"
            ndk {
                abiFilters += listOf("arm64-v8a")
            }
            versionNameSuffix = "-samsung"
        }
    }

    testCoverage {
        jacocoVersion = "0.8.13"
    }

    sourceSets {
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }
}

ksp {
    arg("room.generateKotlin", "true")
    arg("room.incremental", "true")
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.core)
    implementation(platform(libs.kotlinx.serialization.bom))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.material)
    implementation(libs.kotlinx.coroutines.android)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.koin.test.junit4)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.work.testing)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

tasks.register<JacocoReport>("jacocoFullReport") {
    dependsOn("testDebugUnitTest", "connectedDebugAndroidTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    doFirst {
        println("JaCoCo class directories: ${files(kotlinDebugTree, javaDebugTree).files}")
    }

    classDirectories.setFrom(files(kotlinDebugTree, javaDebugTree))
    sourceDirectories.setFrom(files(mainSrc))
    executionData.setFrom(
        fileTree(buildDir) {
            include(
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "outputs/code_coverage/debugAndroidTest/connected/**/*.ec",
            )
        },
    )
}
