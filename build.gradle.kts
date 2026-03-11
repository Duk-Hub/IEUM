plugins {
    java
    id("org.springframework.boot") version "3.5.11"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com"
version = "0.0.1-SNAPSHOT"
description = "IEUM"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

extra["commons-lang3.version"] = "3.18.0"

dependencies {
    //Spring
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    //Security
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    //Jwt
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")

    //DB, Migration
    runtimeOnly("com.mysql:mysql-connector-j")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")

    //Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    //Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.16")

    //Util
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    //Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
