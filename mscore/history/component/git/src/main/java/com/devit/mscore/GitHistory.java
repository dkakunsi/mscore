package com.devit.mscore;

import static com.devit.mscore.util.AttributeConstants.getDomain;
import static com.devit.mscore.util.AttributeConstants.getId;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitHistory implements History {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHistory.class);

    private Git repository;

    private TransportConfigCallback transportConfigCallback;

    private String rootLocalRepo;

    public GitHistory(String rootLocalRepo, Git repository, TransportConfigCallback transportConfigCallback) {
        this.repository = repository;
        this.transportConfigCallback = transportConfigCallback;
        this.rootLocalRepo = rootLocalRepo;
    }

    @Override
    public void create(ApplicationContext context, JSONObject message) throws HistoryException {
        LOGGER.info("BreadcrumbId: {}. Creating history of '{}'", context.getBreadcrumbId(), getId(message));
        try {
            LOGGER.debug("BreadcrumbId: {}. Checkout master branch", context.getBreadcrumbId());
            this.repository.checkout().setName("master").call();

            LOGGER.debug("BreadcrumbId: {}. Pulling from remote repository", context.getBreadcrumbId());
            this.repository.pull().setTransportConfigCallback(transportConfigCallback).call();

            LOGGER.debug("BreadcrumbId: {}. Writing file to local repository", context.getBreadcrumbId());
            writeFile(context, message);

            LOGGER.debug("BreadcrumbId: {}. Add file to Git index", context.getBreadcrumbId());
            this.repository.add().addFilepattern(getFilename(message)).call();

            LOGGER.debug("BreadcrumbId: {}. Committing changes", context.getBreadcrumbId());
            var committer = getCommitter(context);
            var commitMessage = String.format("Changes to '%s' by '%s'", getFilename(message), committer);
            this.repository.commit().setMessage(commitMessage).setAuthor(committer, committer).call();

            LOGGER.debug("BreadcrumbId: {}. Pushing to remote repository", context.getBreadcrumbId());
            this.repository.push().setTransportConfigCallback(transportConfigCallback).call();

            LOGGER.info("BreadcrumbId: {}. Finish creating history of '{}'", context.getBreadcrumbId(), getId(message));
        } catch (GitAPIException ex) {
            LOGGER.error("BreadcrumbId: {}. Error creating history of '{}'", context.getBreadcrumbId(), getId(message));
            throw new HistoryException(ex);
        }
    }

    private static String getCommitter(ApplicationContext context) {
        return StringUtils.isNotBlank(context.getRequestedBy()) ? context.getRequestedBy() : "NONE";
    }

    private void writeFile(ApplicationContext context, JSONObject message) throws HistoryException {
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
            LOGGER.debug("BreadcrumbId: {}. Writing file to '{}'", context.getBreadcrumbId(), filePath);
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
