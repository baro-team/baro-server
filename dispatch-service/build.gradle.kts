plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":common-web"))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
}
