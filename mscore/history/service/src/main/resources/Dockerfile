FROM alpine:latest as packager

RUN apk --no-cache add openjdk11-jdk openjdk11-jmods
RUN apk --no-cache add freetype

ENV JAVA_MINIMAL="/opt/java-minimal"

# build minimal JRE
RUN /usr/lib/jvm/java-11-openjdk/bin/jlink \
    --verbose \
    --add-modules java.base,java.desktop,java.management,java.logging,java.security.sasl,jdk.crypto.cryptoki \
    --compress 2 --strip-debug --no-header-files --no-man-pages \
    --release-info="add:IMPLEMENTOR=radistao:IMPLEMENTOR_VERSION=radistao_JRE" \
    --output "$JAVA_MINIMAL"

FROM alpine:latest

ENV JAVA_HOME=/opt/java-minimal
ENV PATH="$PATH:$JAVA_HOME/bin"

COPY --from=packager "$JAVA_HOME" "$JAVA_HOME"

ENV APP_HOME=/opt/mscore

COPY app.jar $APP_HOME/app.jar
WORKDIR $APP_HOME
RUN set -x && chmod 755 $APP_HOME

EXPOSE 2000
ENTRYPOINT ["java", "-jar", "app.jar"]
