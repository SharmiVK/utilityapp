// Top-level build file
plugins {
    alias(libs.plugins.android.application) apply false

    // UPDATED to 4.4.4 as per your Firebase screen
    id("com.google.gms.google-services") version "4.4.4" apply false
}