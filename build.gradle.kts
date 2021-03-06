import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        maven(url = "https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath("org.jlleitschuh.gradle:ktlint-gradle:9.1.0")
    }
}

plugins {
    id("org.springframework.boot") version "2.7.1"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.asciidoctor.jvm.convert") version "3.3.2"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    kotlin("plugin.jpa") version "1.6.21"
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
    kotlin("kapt") version "1.3.61" // QueryDsl
    idea // QueryDsl
    id("org.sonarqube") version "3.4.0.2513"
}

sonarqube {
    properties {
        property("sonar.projectKey", "SW13-Monstera_backend")
        property("sonar.organization", "sw13-monstera")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

group = "com.csbroker"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

val snippetsDir by extra { file("build/generated-snippets") }
val querydslVersion = "5.0.0"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.jsonwebtoken:jjwt-api:0.11.2")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.2")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.2")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("org.springframework.security:spring-security-test")
    // QueryDsl
    implementation("com.querydsl:querydsl-jpa:$querydslVersion")
    kapt("com.querydsl:querydsl-apt:$querydslVersion:jpa")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.restdocs:spring-restdocs-asciidoctor")
}

idea {
    module {
        val kaptMain = file("build/generated/source/kapt/main")
        sourceDirs.add(kaptMain)
        generatedSourceDirs.add(kaptMain)
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    outputs.dir(snippetsDir)
}

tasks.register("copyYml", Copy::class) {
    copy {
        from("./backend-config")
        include("*.yml")
        into("src/main/resources")
    }
}

tasks.asciidoctor {
    dependsOn(tasks.getByName("copyYml"))
    inputs.dir(snippetsDir)
    dependsOn(tasks.test)
    doFirst { // 2
        delete("src/main/resources/static/docs")
    }
}

tasks.register("copyHTML", Copy::class) { // 3
    dependsOn(tasks.asciidoctor)
    destinationDir = file(".")
    from(tasks.asciidoctor.get().outputDir) {
        into("src/main/resources/static/docs")
    }
}

tasks.bootRun {
    dependsOn(tasks.getByName("copyYml"))
}

tasks.build { // 4
    dependsOn(tasks.getByName("copyYml"))
    dependsOn(tasks.getByName("copyHTML"))
}

tasks.bootJar { // 5
    dependsOn(tasks.asciidoctor)
    from(tasks.asciidoctor.get().outputDir) {
        into("BOOT-INF/classes/static/docs")
    }
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}
