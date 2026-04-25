FROM eclipse-temurin:21-jdk-jammy
RUN mkdir /opt/app
COPY /build/libs/app.jar /opt/app
CMD ["java", "-jar", "/opt/app/app.jar"]