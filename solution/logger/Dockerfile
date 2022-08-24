FROM elastic/filebeat:7.6.2

COPY filebeat.docker.yml /usr/share/filebeat/filebeat.yml
USER root
RUN chown root:filebeat /usr/share/filebeat/filebeat.yml
RUN chmod 744 /usr/share/filebeat/filebeat.yml
USER filebeat