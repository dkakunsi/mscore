package com.devit.mscore;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

import com.devit.mscore.exception.ConfigException;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GitHistoryFactoryTest {

    private static final String PRIVATE_KEY = "src/test/resources/id_rsa";

    private static final String REPO_LOCATION = "repo";

    private static final String HOST_NAME = "github.com";

    private static final String HOST_KEY = "AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBEmKSENjQEezOmxkZMy7opKgwFB9nkt5YRrYMjNuG5N87uRgg6CLrbo5wAdT/y6v0mKV0U2w0WZ2YB/++Tpockg=";

    private static final String TOPICS = "topic1,topic2";

    private Configuration configuration;

    private GitHistoryFactory historyManagerFactory;

    @Before
    public void setup() throws ConfigException {
        this.configuration = mock(Configuration.class);
        doReturn("history").when(this.configuration).getServiceName();
        doReturn(Optional.of(HOST_NAME)).when(this.configuration).getConfig("services.history.git.hostName");
        doReturn(Optional.of(HOST_KEY)).when(this.configuration).getConfig("services.history.git.hostKey");
        doReturn(Optional.of("git@github.com:dkakunsi/git-test.git")).when(this.configuration).getConfig("services.history.git.url");
        doReturn(Optional.of(REPO_LOCATION)).when(this.configuration).getConfig("services.history.git.dir");
        doReturn(Optional.of(PRIVATE_KEY)).when(this.configuration).getConfig("services.history.git.key");
        doReturn(Optional.of("test")).when(this.configuration).getConfig("services.history.git.passPhrase");
        doReturn(Optional.of(TOPICS)).when(this.configuration).getConfig("services.history.topics");
    }

    @After
    public void cleanup() throws IOException {
        if (this.historyManagerFactory != null) {
            this.historyManagerFactory.getRepository().close();
            FileUtils.deleteDirectory(Paths.get(REPO_LOCATION).toFile());
        }
    }

    @Test
    public void testCreateGitHistory() throws ConfigException {
        this.historyManagerFactory = GitHistoryFactory.of(this.configuration);
        var gitHistory = this.historyManagerFactory.historyManager();
        assertNotNull(gitHistory);
    }

    @Test
    public void testOpenGit() throws ConfigException, IllegalStateException, GitAPIException {
        Git.init().setDirectory(Paths.get(REPO_LOCATION).toFile()).call();
        this.historyManagerFactory = GitHistoryFactory.of(this.configuration);
        var gitHistory = this.historyManagerFactory.historyManager();
        assertNotNull(gitHistory);
    }

    @Test
    public void testException() throws ConfigException {
        doReturn(Optional.of("/ :")).when(this.configuration).getConfig("services.history.git.dir");
        assertThrows(ConfigException.class, () -> this.historyManagerFactory = GitHistoryFactory.of(this.configuration));
    }
}
