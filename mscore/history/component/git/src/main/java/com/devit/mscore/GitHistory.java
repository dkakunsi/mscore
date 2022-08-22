package com.devit.mscore;

import static com.devit.mscore.ApplicationContext.getContext;
import static com.devit.mscore.util.AttributeConstants.getDomain;
import static com.devit.mscore.util.AttributeConstants.getId;

import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.logging.ApplicationLogger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.json.JSONObject;

public class GitHistory implements History {

  private static final Logger LOGGER = ApplicationLogger.getLogger(GitHistory.class);

  private Git repository;

  private TransportConfigCallback transportConfigCallback;

  private String rootLocalRepo;

  private GitHistory(String rootLocalRepo, Git repository, TransportConfigCallback transportConfigCallback) {
    this.repository = repository;
    this.transportConfigCallback = transportConfigCallback;
    this.rootLocalRepo = rootLocalRepo;
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void create(JSONObject message) throws HistoryException {
    LOGGER.info("Creating history of '{}'", getId(message));
    try {
      LOGGER.debug("Checkout master branch");
      this.repository.checkout().setName("master").call();

      LOGGER.debug("Pulling from remote repository");
      this.repository.pull().setTransportConfigCallback(transportConfigCallback).call();

      LOGGER.debug("Writing file to local repository");
      writeFile(message);

      LOGGER.debug("Add file to Git index");
      this.repository.add().addFilepattern(getFilename(message)).call();

      LOGGER.debug("Committing changes");
      var committer = getCommitter();
      var commitMessage = String.format("Changes to '%s' by '%s'", getFilename(message), committer);
      this.repository.commit().setMessage(commitMessage).setAuthor(committer, committer).call();

      LOGGER.debug("Pushing to remote repository");
      this.repository.push().setTransportConfigCallback(transportConfigCallback).call();

      LOGGER.info("Finish creating history of '{}'", getId(message));
    } catch (GitAPIException ex) {
      LOGGER.error("Error creating history of '{}'", getId(message));
      throw new HistoryException(ex);
    }
  }

  private static String getCommitter() {
    var context = getContext();
    return StringUtils.isNotBlank(context.getRequestedBy()) ? context.getRequestedBy() : "NONE";
  }

  private void writeFile(JSONObject message) throws HistoryException {
    var filePath = getFilePath(message);

    try {
      if (!Files.exists(filePath)) {
        Files.createDirectories(filePath.getParent());
        Files.createFile(filePath);
      }
    } catch (IOException | NullPointerException ex) {
      throw new HistoryException(ex);
    }

    try (var writer = new FileWriter(filePath.toFile(), StandardCharsets.UTF_8)) {
      LOGGER.debug("Writing file to '{}'", filePath);
      writer.write(message.toString(2));
    } catch (IOException ex) {
      throw new HistoryException(ex);
    }
  }

  private Path getFilePath(JSONObject message) {
    return Paths.get(this.rootLocalRepo, getFilename(message));
  }

  @Override
  public void close() {
    this.repository.close();
  }

  private static String getFilename(JSONObject message) {
    return String.format("%s/%s.json", getDomain(message), getId(message));
  }

  public static class Builder {

    private static final String DEFAULT_LOCAL_REPOSITORY = "./repo";

    private static final String GIT_URL = "services.%s.git.url";

    private static final String LOCAL_REPOSITORY = "services.%s.git.dir";

    private static final String PRIVATE_KEY = "services.%s.git.key";

    private static final String PASS_PHRASE = "services.%s.git.passPhrase";

    private static final String HOST_NAME = "services.%s.git.hostName";

    private static final String HOST_KEY = "services.%s.git.hostKey";

    private Configuration configuration;

    private String localDirectoryPath;

    private Builder(Configuration configuration) throws ConfigException {
      this.configuration = configuration;
      this.localDirectoryPath = getLocalDirectoryPath(configuration);
    }

    public static Builder of(Configuration configuration) throws ConfigException {
      return new Builder(configuration);
    }

    public GitHistory historyManager() throws ConfigException {
      var localDirectoryPath = getLocalDirectoryPath(this.configuration);
      var localDirectory = getLocalDirectory(localDirectoryPath);
      var repository = initRepository(this.configuration, localDirectory);
      return historyManager(this.localDirectoryPath, repository, createTransportConfigCallback(this.configuration));
    }

    public GitHistory historyManager(String directoryPath, Git repository, TransportConfigCallback transportConfigCallback) throws ConfigException {
      return new GitHistory(directoryPath, repository, transportConfigCallback);
    }

    private static File getLocalDirectory(String directoryPath) throws ConfigException {
      return Paths.get(directoryPath).toFile();
    }

    private static String getLocalDirectoryPath(Configuration configuration) throws ConfigException {
      return getConfig(configuration, LOCAL_REPOSITORY).orElse(DEFAULT_LOCAL_REPOSITORY);
    }

    static Git initRepository(Configuration configuration, File repoDir) throws ConfigException {
      try {
        if (isLocalRepositoryExists(repoDir)) {
          return Git.open(repoDir);
        } else {
          var dirCreated = repoDir.mkdir();
          if (!dirCreated) {
            throw new ApplicationRuntimeException("Cannot create local directory");
          }
          var transportConfigCallback = createTransportConfigCallback(configuration);
          return cloneRepository(configuration, repoDir, transportConfigCallback);
        }
      } catch (Exception ex) {
        throw new ConfigException(ex);
      }
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
      var configKey = String.format(key, configuration.getServiceName());
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
  }
}
