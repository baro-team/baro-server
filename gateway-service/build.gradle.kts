plugins {
    id("org.springframework.boot")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.3")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("org.springframework.security:spring-security-oauth2-jose")

    testImplementation("io.projectreactor:reactor-test")
}
