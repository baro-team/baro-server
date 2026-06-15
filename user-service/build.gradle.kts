plugins {
    id("org.springframework.boot")
    kotlin("plugin.jpa")
}

dependencies {
    implementation(project(":common-core"))
    implementation(project(":common-web"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.security:spring-security-crypto")
    implementation("org.springframework.security:spring-security-oauth2-jose")

    runtimeOnly("org.postgresql:postgresql")

    testRuntimeOnly("com.h2database:h2")
}
