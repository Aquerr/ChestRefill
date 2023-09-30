import net.minecraftforge.gradle.userdev.UserDevExtension
import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency
import java.io.ByteArrayOutputStream

buildscript {
    repositories {
        maven { url = uri("https://maven.minecraftforge.net") }
        mavenCentral()
    }
    dependencies {
        classpath(group = "net.minecraftforge.gradle", name = "ForgeGradle", version = "5.1.+") {
            isChanging = true
        }
    }
}

val chestRefillId = findProperty("chestrefill.id") as String
val chestRefillName = findProperty("chestrefill.name") as String
val chestRefillVersion = findProperty("chestrefill.version") as String
val minecraftVersion = findProperty("minecraft.version") as String
val forgeVersion = findProperty("forge.version") as String
val spongeApiVersion = findProperty("sponge-api.version") as String

plugins {
    idea
    java
    `maven-publish`
    id("org.spongepowered.gradle.plugin") version "2.1.1"
    id("org.spongepowered.gradle.ore") version "2.1.1" // for Ore publishing
}

group = "io.github.aquerr"
version = "$chestRefillVersion-API-$spongeApiVersion"
apply(plugin = "net.minecraftforge.gradle")

description = "Plugin for restoring contents of a container after the specified time."

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType(JavaCompile::class).configureEach {
    options.apply {
        encoding = "utf-8" // Consistent source file encoding
    }
}

// Make sure all tasks which produce archives (jar, sources jar, javadoc jar, etc) produce more consistent output
tasks.withType(AbstractArchiveTask::class).configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}

dependencies {
    "minecraft"("net.minecraftforge:forge:${forgeVersion}")
    api("org.spongepowered:spongeapi:${spongeApiVersion}")
}

tasks {
    jar {
        finalizedBy("reobfJar")

        if(System.getenv("JENKINS_HOME") != null) {
            project.version = project.version.toString() + "_" + System.getenv("BUILD_NUMBER")
            println("File name => " + archiveBaseName.get())
        } else {
            project.version = project.version.toString() + "-SNAPSHOT"
        }
    }
}

configure<UserDevExtension> {
    mappings("official", minecraftVersion)
}

sponge {
    apiVersion(spongeApiVersion)
    license("MIT")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin(chestRefillId) {
        displayName(chestRefillName)
        version(chestRefillVersion)
        entrypoint("io.github.aquerr.chestrefill.ChestRefill")
        description("Rebuilds destroyed blocks after specified time.")
        links {
            homepage("https://github.com/Aquerr/ChestRefill")
            source("https://github.com/Aquerr/ChestRefill")
            issues("https://github.com/Aquerr/ChestRefill/issues")
        }
        contributor("Aquerr") {
            description("Lead Developer")
        }
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
    }
}

val getGitCommitDesc by tasks.registering(Exec::class) {
    commandLine("git", "log", "-1", "--pretty=%B")
    standardOutput = ByteArrayOutputStream()
    doLast {
        project.extra["gitCommitDesc"] = standardOutput.toString()
    }
}

tasks.register("printEnvironment") {
    doLast {
        System.getenv().forEach { key, value ->
            println("$key -> $value")
        }
    }
}

tasks.create("publishBuildOnDiscord") {
    dependsOn(getGitCommitDesc)
    group = "Publishing"
    description = "Task for publishing the jar file to discord's jenkins channel"
    doLast {
        val jarFiles: List<String> = groovy.ant.FileNameFinder().getFileNames(project.buildDir.path, "**/*.jar")

        if(jarFiles.size > 0) {
            println("Found jar files: " + jarFiles)

            var lastCommitDescription = project.extra["gitCommitDesc"]
            if(lastCommitDescription == null || lastCommitDescription == "") {
                lastCommitDescription = "No changelog provided"
            }

            exec {
                commandLine("java", "-jar",  ".." + File.separator + "jenkinsdiscordbot-1.0.jar", "ChestRefill", jarFiles[0], lastCommitDescription)
            }
        }
    }
}

oreDeployment {
    // The default publication here is automatically configured by SpongeGradle
    // using the first-created plugin's ID as the project ID
    // A version body is optional, to provide additional information about the release
    /*
    defaultPublication {
        // Read the version body from the file whose path is provided to the changelog gradle property
        versionBody.set(providers.gradleProperty("changelog").map { file(it).readText(Charsets.UTF_8) }.orElse(""))
    }*/
}

publishing {

    repositories {
        maven {
            name = "GithubPackages"
            url = uri("https://maven.pkg.github.com/Aquerr/ChestRefill")
            credentials {
                username = System.getenv("GITHUB_PUBLISHING_USERNAME")
                password = System.getenv("GITHUB_PUBLISHING_TOKEN")
            }
        }
    }

    publications {
        register<MavenPublication>(chestRefillId) {
            artifactId = chestRefillId
            description = project.description

            from(components["java"])
        }
    }
}