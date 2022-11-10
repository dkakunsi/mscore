package com.devit.mscore.notification.mail;

public final class SendInfo {

  private String host;

  private String port;

  private String user;

  private String password;

  private String from;

  private String subject;

  public SendInfo(String host, String port, String user, String password, String from, String subject) {
    this(host, port, from, subject);
    this.user = user;
    this.password = password;
  }

  public SendInfo(String host, String port, String from, String subject) {
    this.host = host;
    this.port = port;
    this.from = from;
    this.subject = subject;
  }

  public String getHost() {
    return host;
  }

  public String getPort() {
    return port;
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }

  public String getFrom() {
    return from;
  }

  public String getSubject() {
    return subject;
  }
}
