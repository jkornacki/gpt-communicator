
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY . .
RUN mvn clean package

FROM eclipse-temurin:21-jdk-ubi9-minimal
ARG JVM_OPTS
WORKDIR /app
COPY --from=builder /build/target/gpt-demo.jar /app/app.jar
ENV JAVA_OPTS='-Djava.net.preferIPv4Stack=true -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:7180 -Dspring.datasource.url=jdbc:h2:file:./db/data.sb -Dspring.h2.console.settings.web-allow-others=true -Djava.security.egd=file:/dev/./urandom -Xms256m -Xmx1024m'

EXPOSE 7070
EXPOSE 7180
ENTRYPOINT [ "sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar" ]
