package com.devit.mscore;

import java.util.Base64;

import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.util.FS;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

public class CustomJschConfigSessionFactory extends JschConfigSessionFactory {

  private final String hostName;

  private final String hostKey;

  private final String keyLocation;

  private final String passPhrase;

  public CustomJschConfigSessionFactory(String hostName, String hostKey, String keyLocation, String passPhrase) {
    this.hostName = hostName;
    this.hostKey = hostKey;
    this.keyLocation = keyLocation;
    this.passPhrase = passPhrase;
  }

  @Override
  protected JSch createDefaultJSch(FS fs) throws JSchException {
    var jsch = super.createDefaultJSch(fs);
    addHostKey(jsch);
    jsch.removeAllIdentity();
    jsch.addIdentity(this.keyLocation, this.passPhrase);
    return jsch;
  }

  private void addHostKey(JSch jsch) throws JSchException {
    var key = Base64.getDecoder().decode(this.hostKey);
    var hostKey = new HostKey(this.hostName, key);
    jsch.getHostKeyRepository().add(hostKey, null);
  }
}
