val cloudVersion: String by project

description = "Minecraft Crowd Control: Paper"

repositories {
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    implementation(project(":base-common"))
    implementation("cloud.commandframework:cloud-paper:${cloudVersion}")
    compileOnly("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")
}

// Java 17 boilerplate

val targetJavaVersion = 17
tasks.withType<JavaCompile>().configureEach {
    options.release.set(targetJavaVersion)
}

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
//    withSourcesJar()
}
