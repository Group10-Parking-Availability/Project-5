// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
}

// For some reason the chart library would not work without a buildscript
buildscript {
    val agp_version by extra("8.2.2")
    dependencies {
        classpath("com.android.tools.build:gradle:$agp_version")

    }
}

