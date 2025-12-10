FROM eclipse-temurin:21-jdk-jammy
VOLUME /tmp
COPY ServerSprintEyeGit/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar","/app.jar"]
