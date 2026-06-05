plugins {
    id("org.springframework.boot")
    kotlin("plugin.jpa")
}

dependencies {
    implementation(project(":common-kakao"))
    implementation(project(":common-web"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    implementation(kotlin("stdlib"))

    runtimeOnly("org.postgresql:postgresql")

    testRuntimeOnly("com.h2database:h2")
}
repositories {
    mavenCentral()
}