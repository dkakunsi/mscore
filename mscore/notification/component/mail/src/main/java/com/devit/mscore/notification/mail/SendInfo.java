package com.devit.mscore.notification.mail;

import com.devit.mscore.Configuration;

public class SendInfo {

    private String host;

    private String port;

    private String user;

    private String password;

    private String from;

    private String subject;

    public SendInfo() {
        super();
    }

    public SendInfo(Configuration configuration) {
        this.host = configuration.getConfig("mail.host");
        this.port = configuration.getConfig("mail.port");
        this.from = configuration.getConfig("mail.from");
        this.subject = configuration.getConfig("mail.subject");

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
