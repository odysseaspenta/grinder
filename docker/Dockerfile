# Console image. Build the distribution first, copy the zip next to this
# file, then build with this directory as the context:
#
#   mvn -B -DskipTests package
#   cp grinder/target/grinder-*-binary.zip docker/
#   docker build docker/ -t grinder:local
#   docker run --rm -p 6372:6372 -p 6373:6373 grinder:local

FROM eclipse-temurin:21-jdk-alpine AS build
COPY grinder-*-binary.zip /tmp/dist/
RUN mkdir /tmp/unpacked && cd /tmp/unpacked && jar xf /tmp/dist/*.zip && \
    mv /tmp/unpacked/grinder-* /opt/grinder
# A runtime with every Java SE module (scripts can touch any of them) plus
# what Jython, the DCR agent and TLS need, instead of the full JDK image.
RUN jlink --add-modules java.se,jdk.unsupported,jdk.crypto.ec,jdk.zipfs,jdk.localedata,jdk.jcmd,jdk.management.agent,jdk.naming.dns,jdk.charsets \
          --strip-debug --no-man-pages --no-header-files --compress zip-6 \
          --output /opt/jre

FROM alpine:3.21

ENV JAVA_HOME=/opt/java
ENV PATH=/opt/java/bin:${PATH}
COPY --from=build /opt/jre /opt/java

ENV GRINDER_ROOT=/opt/grinder
ENV GRINDER_WORKSPACE=/opt/grinder/workspace

COPY --from=build /opt/grinder ${GRINDER_ROOT}

RUN mkdir ${GRINDER_WORKSPACE} && \
    touch ${GRINDER_WORKSPACE}/setGrinderEnv.sh && \
    echo "grinder.console.propertiesFile=${GRINDER_WORKSPACE}/grinder.properties" > ~/.grinder_console && \
    echo "grinder.console.scriptDistributionDirectory=${GRINDER_WORKSPACE}" >> ~/.grinder_console

WORKDIR ${GRINDER_ROOT}

# 6372: agents and workers. 6373: REST API and web console.
EXPOSE 6372 6373

CMD . ${GRINDER_WORKSPACE}/setGrinderEnv.sh && java -cp ${CLASSPATH}:lib/grinder.jar ${GRINDER_PROPERTIES} net.grinder.Console
