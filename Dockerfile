FROM maven:3.6.3-jdk-11-slim

MAINTAINER Emanuel Almirante, emanuelalmirante@gmail.com

COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

VOLUME /tmp
ARG JAR_FILE=target/exercise-wallet-0.0.2.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

EXPOSE 8090:8090