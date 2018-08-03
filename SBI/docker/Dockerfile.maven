FROM maven:3.5.0-jdk-8
ADD ./target/my-app-1.0-SNAPSHOT.jar /tmp/my-app-1.0-SNAPSHOT.jar
EXPOSE 8080
CMD ["java", "-jar", "/tmp/my-app-1.0-SNAPSHOT.jar"]
