FROM maven:3.9.6-amazoncorretto-21
WORKDIR /boot/target
COPY /boot/target/intervention-service-0.0.1-SNAPSHOT.jar intervention-service-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "intervention-service-0.0.1-SNAPSHOT.jar"]