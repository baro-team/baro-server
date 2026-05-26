plugins {
    `java-library`
}

dependencies {
    api(project(":common-web"))
    api("jakarta.servlet:jakarta.servlet-api")
    api("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    api("org.springframework.boot:spring-boot-starter-security")
    api("org.springframework.security:spring-security-oauth2-jose")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}
