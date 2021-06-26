#
# Build stage
#
FROM maven:3.6.0-jdk-11-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

FROM mcr.microsoft.com/java/jdk:11-zulu-alpine
CMD uname
RUN apk update
RUN apk add git
RUN apk add autoconf
RUN apk add build-base
RUN apk add gcc
RUN apk add libxslt

RUN mkdir -p /opt && cd /opt && git clone https://github.com/jemalloc/jemalloc.git \
    && mkdir /tmp/jprof && mkdir /tmp/nmt && mkdir /tmp/pmap \
    && mkdir /diagnostic

RUN cd /opt/jemalloc && git checkout -b stable-4 origin/stable-4
RUN cd /opt/jemalloc && ./autogen.sh --enable-prof

RUN cd /opt/jemalloc && make
RUN cd /opt/jemalloc && make install_bin install_include install_lib

RUN addgroup -S intakegroup && adduser -S intakeuser -G intakegroup
USER intakeuser

#ENV LD_PRELOAD="/usr/local/lib/libjemalloc.so"
ENV MALLOC_CONF="prof_leak:true,prof:true,lg_prof_interval:25,lg_prof_sample:18,prof_prefix:/tmp/jeprof"

COPY --from=build /home/app/target/azsdk-supportability-eventhubs-0.0.1-SNAPSHOT.jar azsdk-supportability-eventhubs-0.0.1-SNAPSHOT.jar
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
"-XX:NativeMemoryTracking=detail", \
## The executable
"-jar", \
"/azsdk-supportability-eventhubs-0.0.1-SNAPSHOT.jar"]

## Run this file using command
## docker build -t main .
## docker run -p 9010:9010 -it main
