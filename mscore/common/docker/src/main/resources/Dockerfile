# This is the docker template for all mscore services.

FROM devit16/mscore

ENV APP_HOME=/opt/mscore

COPY app.jar $APP_HOME/app.jar
WORKDIR $APP_HOME
RUN set -x && chmod 755 $APP_HOME

EXPOSE 2000
ENTRYPOINT ["java", "-jar", "app.jar"]
