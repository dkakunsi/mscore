FROM devit16/mscore-notification-service

COPY ./template $APP_HOME/template
COPY *.config $APP_HOME/
COPY *.properties $APP_HOME/

ENTRYPOINT exec java $JAVA_OPTS -Dlog4j2.configurationFile=./log4j2.properties -jar app.jar
