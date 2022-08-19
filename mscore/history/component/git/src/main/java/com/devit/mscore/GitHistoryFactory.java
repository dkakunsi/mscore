package com.devit.mscore;

import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.ConfigException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;

public class GitHistoryFactory {

  private static final String DEFAULT_LOCAL_REPOSITORY = "./repo";

  private static final String CONFIG_TEMPLATE = "services.%s.git.%s";

  private static final String GIT_URL = "url";

  private static final String LOCAL_REPOSITORY = "dir";

  private static final String PRIVATE_KEY = "key";

  private static final String PASS_PHRASE = "passPhrase";

  private static final String HOST_NAME = "hostName";

  private static final String HOST_KEY = "hostKey";

  private Configuration configuration;

  private Git repository;

  GitHistoryFactory(Configuration configuration, Git repository) {
    this.configuration = configuration;
    this.repository = repository;
  }

  public static GitHistoryFactory of(Configuration configuration) throws ConfigException {
    try {
      var repoDir = getRepoDirectory(configuration);

      Git repository;
      if (isLocalRepositoryExists(repoDir)) {
        repository = Git.open(repoDir);
      } else {
        repoDir.mkdir();
        repository = cloneRepository(configuration, repoDir,
            createTransportConfigCallback(configuration));
      }
      return new GitHistoryFactory(configuration, repository);
    } catch (Exception ex) {
      throw new ConfigException(ex);
    }
  }

  private static File getRepoDirectory(Configuration configuration)
      throws ConfigException {
    var gitDir = getLocalDirectory(configuration);
    return Paths.get(gitDir).toFile();
  }

  private static String getLocalDirectory(Configuration configuration) throws ConfigException {
    return getConfig(configuration, LOCAL_REPOSITORY).orElse(DEFAULT_LOCAL_REPOSITORY);
  }

  static boolean isLocalRepositoryExists(File repoDir) {
    return Files.exists(Paths.get(repoDir.toString(), ".git"));
  }

  static Git cloneRepository(Configuration configuration, File repoDir,
      TransportConfigCallback transportConfigCallback)
      throws ConfigException {

    var localTransportConfigCallback = transportConfigCallback != null ? transportConfigCallback
        : createTransportConfigCallback(configuration);

    try {
      return Git.cloneRepository().setTransportConfigCallback(localTransportConfigCallback)
          .setURI(getGitUrl(configuration)).setDirectory(repoDir).call();
    } catch (GitAPIException ex) {
      throw new ConfigException(ex);
    }
  }

  private static String getGitUrl(Configuration configuration) throws ConfigException {
    return getConfig(configuration, GIT_URL).orElseThrow(() -> new ConfigException("No git uri is provided."));
  }

  private static Optional<String> getConfig(Configuration configuration, String key) throws ConfigException {
    var configKey = String.format(CONFIG_TEMPLATE, configuration.getServiceName(), key);
    return configuration.getConfig(configKey);
  }

  static TransportConfigCallback createTransportConfigCallback(Configuration configuration) {
    return transport -> {
      try {
        var sshTransport = (SshTransport) transport;
        sshTransport.setSshSessionFactory(createSshSessionFactory(configuration));
      } catch (ConfigException ex) {
        throw new ApplicationRuntimeException(ex);
      }
    };
  }

  private static SshSessionFactory createSshSessionFactory(Configuration configuration) throws ConfigException {

    var hostName = getHostName(configuration);
    var hostKey = getHostKey(configuration);
    var privateKey = getPrivateKey(configuration);
    var passphrase = getPassphrase(configuration);

    return new CustomJschConfigSessionFactory(hostName, hostKey, privateKey, passphrase);
  }

  private static String getHostName(Configuration configuration) throws ConfigException {
    return getConfig(configuration, HOST_NAME).orElseThrow(() -> new ConfigException("No host name configured."));
  }

  private static String getHostKey(Configuration configuration) throws ConfigException {
    return getConfig(configuration, HOST_KEY).orElseThrow(() -> new ConfigException("No host key configured."));
  }

  private static String getPrivateKey(Configuration configuration) throws ConfigException {
    return getConfig(configuration, PRIVATE_KEY).orElseThrow(() -> new ConfigException("No private key configured."));
  }

  private static String getPassphrase(Configuration configuration) throws ConfigException {
    return getConfig(configuration, PASS_PHRASE).orElseThrow(() -> new ConfigException("No passphrase configured."));
  }

  public GitHistory historyManager() throws ConfigException {
    var gitDir = getLocalDirectory(this.configuration);
    return new GitHistory(gitDir, this.repository, createTransportConfigCallback(this.configuration));
  }

  Git getRepository() {
    return this.repository;
  }
}
