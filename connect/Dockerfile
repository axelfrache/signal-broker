FROM maven:3.9-eclipse-temurin-17 AS smt-builder

WORKDIR /build
COPY discord-smt /build/discord-smt
RUN cd discord-smt && mvn clean package -DskipTests

FROM confluentinc/cp-kafka-connect:7.9.0

RUN confluent-hub install --no-prompt confluentinc/kafka-connect-jdbc:10.7.6
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-s3:latest
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-http:1.7.7

# Copy custom SMT
COPY --from=smt-builder /build/discord-smt/target/discord-smt-1.0.0.jar /usr/share/java/kafka-connect-http/
