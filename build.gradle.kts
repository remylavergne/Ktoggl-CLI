import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.31"
    kotlin("plugin.serialization") version "1.4.31"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    application
}

group = "dev.remylavergne"
version = "0.0.4-alpha"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.0")

    implementation(project(":Ktoggl"))

    implementation("com.github.ajalt.clikt:clikt:3.1.0")
    testImplementation("io.kotest:kotest-runner-junit5:4.4.3")
    testImplementation(kotlin("test-junit"))
    testImplementation("io.kotest:kotest-assertions-core:4.4.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0-RC")

    // Excel
    implementation("org.apache.poi:poi-ooxml:4.1.2")
}

tasks.test {
    useJUnit()
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClassName = "dev.remylavergne.togglsheet.MainKt"
}

tasks.shadowJar {
    archiveBaseName.set("ktoggl-cli")
    archiveClassifier.set("")
    archiveVersion.set(version)
    manifest {
        attributes(mapOf("Main-Class" to application.mainClassName))
    }
    minimize()
}
