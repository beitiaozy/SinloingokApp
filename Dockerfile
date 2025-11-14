FROM openjdk:8-jre

WORKDIR /sinloingok
COPY target/sinloingok-2.0-SNAPSHOT.jar sinloingok.jar


EXPOSE 8080 443 18080
ENTRYPOINT ["java", "-jar", "sinloingok.jar"]