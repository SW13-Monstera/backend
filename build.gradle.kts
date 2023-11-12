import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        maven(url = "https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath("org.jlleitschuh.gradle:ktlint-gradle")
    }
}

plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")

    idea
    jacoco

    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.asciidoctor.jvm.convert")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.sonarqube")
}

sonarqube {
    properties {
        property("sonar.projectKey", "SW13-Monstera_backend")
        property("sonar.organization", "sw13-monstera")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

group = "${property("projectGroup")}"
version = "${property("applicationVersion")}"
java.sourceCompatibility = JavaVersion.valueOf("VERSION_${property("javaVersion")}")

repositories {
    mavenCentral()
}

val snippetsDir by extra { file("build/generated-snippets") }

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.jsonwebtoken:jjwt-api:${property("jwtVersion")}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${property("jwtVersion")}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${property("jwtVersion")}")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("com.h2database:h2")
    implementation("mysql:mysql-connector-java")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("org.springframework.restdocs:spring-restdocs-restassured")
    testImplementation("io.rest-assured:spring-mock-mvc")
    testImplementation("org.springframework.security:spring-security-test")

    // QueryDsl
    implementation("com.querydsl:querydsl-jpa:${property("queryDslVersion")}")
    kapt("com.querydsl:querydsl-apt:${property("queryDslVersion")}:jpa")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.restdocs:spring-restdocs-asciidoctor")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    testImplementation("io.mockk:mockk:${property("mockkVersion")}")
    implementation("com.squareup.okhttp3:okhttp:${property("okHttpVersion")}")
    testImplementation("com.squareup.okhttp3:mockwebserver:${property("okHttpVersion")}")
    testImplementation("io.kotest:kotest-runner-junit5:${property("kotestVersion")}")
    testImplementation("io.kotest:kotest-assertions-core:${property("kotestVersion")}")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:${property("kotestExtensionsVersion")}")

    implementation("io.sentry:sentry-spring-boot-starter:${property("sentryVersion")}")

    // mail
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("aws.sdk.kotlin:ses:${property("awsSdkVersion")}")
    implementation("aws.sdk.kotlin:s3:${property("awsSdkVersion")}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${property("kotlinCoroutinesVersion")}")

    // rate-limiter
    implementation("com.giffing.bucket4j.spring.boot.starter:bucket4j-spring-boot-starter:${property("bucket4jVersion")}")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudDependenciesVersion")}")
    }
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
        jvmTarget = "${project.property("javaVersion")}"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        html.isEnabled = true
        html.destination = file("$buildDir/reports/myReport.html")
        csv.isEnabled = true
        xml.isEnabled = true
    }

    var excludes = mutableListOf<String>()
    excludes.add("io/csbroker/apiserver/model")
    excludes.add("io/csbroker/apiserver/common")
    excludes.add("io/csbroker/apiserver/dto")
    excludes.add("io/csbroker/apiserver/ApiServerApplication.kt")

    classDirectories.setFrom(
        sourceSets.main.get().output.asFileTree.matching {
            exclude(excludes)
        },
    )
}

tasks.test {
    outputs.dir(snippetsDir)
    finalizedBy(tasks.jacocoTestReport)
}

tasks.register("copyYml", Copy::class) {
    copy {
        from("./backend-config")
        include("*.yml", "*.xml")
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

val jar: Jar by tasks

jar.enabled = false
