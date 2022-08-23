FROM alpine:latest as packager

RUN apk --no-cache add openjdk11-jdk openjdk11-jmods
RUN apk --no-cache add freetype

ENV JAVA_MINIMAL="/opt/java-minimal"

# build minimal JRE
RUN /usr/lib/jvm/java-11-openjdk/bin/jlink \
    --verbose \
    --add-modules java.base,java.desktop,java.management,java.logging,java.security.sasl,java.sql,java.naming,java.scripting \
    --compress 2 --strip-debug --no-header-files --no-man-pages \
    --release-info="add:IMPLEMENTOR=radistao:IMPLEMENTOR_VERSION=radistao_JRE" \
    --output "$JAVA_MINIMAL"

RUN apk add --no-cache fontconfig ttf-dejavu && fc-cache -f && \
    ln -s /usr/lib/libfontconfig.so.1 /usr/lib/libfontconfig.so && \
    ln -s /lib/libuuid.so.1 /usr/lib/libuuid.so.1 && \
    ln -s /lib/libc.musl-x86_64.so.1 /usr/lib/libc.musl-x86_64.so.1

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
