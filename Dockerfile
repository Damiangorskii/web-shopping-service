FROM openjdk:17-jdk

WORKDIR /app

LABEL maintainer="damian" \
      version="1.0" \
      description="Docker image for the web-shopping-service"

COPY target/web-shopping-service-0.0.1-SNAPSHOT.jar /app/web-shopping-service.jar

EXPOSE 8087

CMD ["java", "-jar", "/app/web-shopping-service.jar"]