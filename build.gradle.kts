plugins {
    java
}

group = "de.tmxx"
version = "1.0.0"

repositories {
    mavenCentral()

    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        name = "jitpack.io"
        url = uri("https://jitpack.io")
    }
}

dependencies {
    // papermc
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    // google guice
    compileOnly("com.google.inject:guice:7.0.0")
    compileOnly("com.google.inject.extensions:guice-assistedinject:7.0.0")

    // vault
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude("org.bukkit", "bukkit")
    }
}

java {
    // setting the language version explicitly to 21 as paper needs that version to run
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.jar {
    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }
}