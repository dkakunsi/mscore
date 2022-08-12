package com.devit.mscore.notification.mail;

import com.devit.mscore.Configuration;
import com.devit.mscore.exception.ConfigException;

public class SendInfo {

    private static final String MAIL_HOST = "platform.mail.host";

    private static final String MAIL_PORT = "platform.mail.port";

    private static final String MAIL_FROM = "platform.mail.from";

    private static final String MAIL_SUBJECT = "services.%s.email.subject";

    private String host;

    private String port;

    private String user;

    private String password;

    private String from;

    private String subject;

    public SendInfo() {
        super();
    }

    public SendInfo(Configuration configuration) throws ConfigException {
        var serviceName = configuration.getServiceName();
        this.host = configuration.getConfig(MAIL_HOST).orElseThrow(() -> new ConfigException("No mailing host provided"));
        this.port = configuration.getConfig(MAIL_PORT).orElseThrow(() -> new ConfigException("No mailing port provided"));
        this.from = configuration.getConfig(MAIL_FROM).orElseThrow(() -> new ConfigException("No mailing from address provided"));
        this.subject = configuration.getConfig(String.format(MAIL_SUBJECT, serviceName)).orElseThrow(() -> new ConfigException("No mailing subject provided"));

    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
