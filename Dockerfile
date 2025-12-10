FROM eclipse-temurin:17-jdk-jammy
VOLUME /tmp
COPY ServerSprintEyeGit/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar","/app.jar"]
