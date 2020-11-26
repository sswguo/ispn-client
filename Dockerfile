FROM openjdk:8

COPY ./target/ispn-client-0.0.1-SNAPSHOT.jar  /var/lib/ispn/
COPY ./certs/tls.crt /var/lib/ispn/
WORKDIR /var/lib/ispn/

EXPOSE 8080
CMD ["java", "-jar", "ispn-client-0.0.1-SNAPSHOT.jar"]
