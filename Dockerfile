FROM amazoncorretto:21-alpine3.18-jdk

COPY rest/target/rest-0.0.1-SNAPSHOT.jar authentication-app.jar

ENTRYPOINT ["java","-jar","/authentication-app.jar"]
