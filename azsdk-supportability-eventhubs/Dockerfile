FROM mcr.microsoft.com/java/jre-headless:11-zulu-alpine
RUN addgroup -S intakegroup && adduser -S intakeuser -G intakegroup
USER intakeuser
ARG JAR_FILE=target/azsdk-supportability-eventhubs-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} azsdk-supportability-eventhubs-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", \

## These allow VisualVM to connect to the JVM and profile the app
"-Dcom.sun.management.jmxremote=true", \
"-Dcom.sun.management.jmxremote.port=9010", \
"-Dcom.sun.management.jmxremote.local.only=false", \
"-Dcom.sun.management.jmxremote.authenticate=false", \
"-Dcom.sun.management.jmxremote.ssl=false", \
"-Dcom.sun.management.jmxremote.rmi.port=9010", \
"-Djava.rmi.server.hostname=localhost", \

## Recommended to include these JVM flags to get better JVM ergonomics tuning when running the app in a container
"-XX:+UnlockExperimentalVMOptions", \
"-XX:MaxRAMPercentage=50.0", \
"-XX:+UseContainerSupport", \

## The executable
"-jar", \
"/azsdk-supportability-eventhubs-0.0.1-SNAPSHOT.jar"]

## Run this file using command
## docker build -t main .
## docker run -p 9010:9010 -it main
