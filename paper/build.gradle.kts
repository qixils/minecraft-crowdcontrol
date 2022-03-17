val cloudVersion: String by project

repositories {
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    implementation(project(":common-platform"))
    implementation("cloud.commandframework:cloud-paper:${cloudVersion}")
    compileOnly("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")
}

description = "Minecraft Crowd Control: Paper"
