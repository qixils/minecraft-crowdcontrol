plugins {
    kotlin("jvm") version "1.6.10" apply true
    id("io.freefair.lombok") version "6.4.1" apply false
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("io.freefair.lombok")
    }

    repositories {
        mavenCentral()
        mavenLocal() // TODO remove -- included to allow builds w/ 1.7.0-SNAPSHOT
    }

    // TODO: shading
    // TODO: auto-copy default config into appropriate resource paths
    // (i.e. /config.yml for Paper, /assets/crowd-control/default.conf for Sponge 7, etc.)
    // TODO: unrelated to this file but output files should have custom filenames
    // TODO: also unrelated to this file but GH workflows need updating
}