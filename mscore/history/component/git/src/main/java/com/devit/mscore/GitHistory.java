package com.devit.mscore;

import static com.devit.mscore.ApplicationContext.getContext;
import static com.devit.mscore.util.AttributeConstants.getDomain;
import static com.devit.mscore.util.AttributeConstants.getId;

import com.devit.mscore.logging.ApplicationLogger;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONObject;

public class GitHistory implements History {

  private static final Logger LOGGER = ApplicationLogger.getLogger(GitHistory.class);

  private Git repository;

  private TransportConfigCallback transportConfigCallback;

  private String rootLocalRepo;

  public GitHistory(String rootLocalRepo, Git repository, TransportConfigCallback transportConfigCallback) {
    this.repository = repository;
    this.transportConfigCallback = transportConfigCallback;
    this.rootLocalRepo = rootLocalRepo;
  }

  @Override
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
    } catch (IOException ex) {
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

  private static String getFilename(JSONObject message) {
    return String.format("%s/%s.json", getDomain(message), getId(message));
  }
}
