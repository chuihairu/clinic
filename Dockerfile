FROM openjdk:17-jdk-alpine
RUN mkdir /app && mkdir -p /app/logs && mkdir -p /app/config
VOLUME /tmp

ARG JAVA_OPTS
ENV JAVA_OPTS=$JAVA_OPTS
COPY target/clinic-server.jar /app/server.jar
EXPOSE 2347
ENTRYPOINT exec java $JAVA_OPTS -jar server.jar
# For Spring-Boot project, use the entrypoint below to reduce Tomcat startup time.
#ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar clinic.jar
