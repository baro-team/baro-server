plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":common-kakao"))
    implementation(project(":common-security"))
    implementation(project(":common-web"))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")

    runtimeOnly("org.postgresql:postgresql")

    testRuntimeOnly("com.h2database:h2")
}
