plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-integration")
    implementation("org.springframework.integration:spring-integration-mqtt")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.78.1")
}
