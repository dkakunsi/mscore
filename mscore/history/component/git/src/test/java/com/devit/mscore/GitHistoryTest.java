package com.devit.mscore;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.lib.Repository;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class GitHistoryTest {

    private GitHistory gitHistory;

    private Git git;

    private ApplicationContext context;

    @Before
    public void setup() {
        this.context = DefaultApplicationContext.of("test");

        var repoDir = Paths.get(".").toFile();
        var repository = mock(Repository.class);
        doReturn(repoDir).when(repository).getDirectory();

        this.git = mock(Git.class);
        doReturn(repository).when(this.git).getRepository();

        var transportConfigCallback = mock(TransportConfigCallback.class);

        this.gitHistory = new GitHistory("repo", this.git, transportConfigCallback);
    }

    @After
    public void cleanup() throws IOException {
        FileUtils.deleteDirectory(Paths.get("domain").toFile());
    }

    @Test
    public void testCreate() throws HistoryException {
        mockCheckout(this.git);
        mockPull(this.git);
        mockAdd(this.git);
        mockCommit(this.git);
        mockPush(this.git);

        var message = new JSONObject();
        message.put("domain", "domain");
        message.put("id", "id");
        message.put("name", "name");

        try (MockedStatic<ApplicationContext> utilities = Mockito.mockStatic(ApplicationContext.class)) {
            utilities.when(() -> ApplicationContext.getContext()).thenReturn(context);

            this.gitHistory.create(message);
        }
    }

    @Test
    public void testCreate_ThrowsException() throws HistoryException, GitAPIException {
        mockCheckout(this.git);
        mockPull(this.git);
        mockAdd(this.git);
        mockCommit(this.git);
        mockPushException(this.git);

        var message = new JSONObject();
        message.put("domain", "domain");
        message.put("id", "id");
        message.put("name", "name");

        try (MockedStatic<ApplicationContext> utilities = Mockito.mockStatic(ApplicationContext.class)) {
            utilities.when(() -> ApplicationContext.getContext()).thenReturn(context);

            var ex = assertThrows(HistoryException.class, () -> this.gitHistory.create(message));
            var cause = ex.getCause();
            assertThat(cause, instanceOf(GitAPIException.class));
        }
    }

    private static void mockCheckout(Git repository) {
        var checkoutCommand = mock(CheckoutCommand.class);
        doReturn(checkoutCommand).when(checkoutCommand).setName(anyString());

        doReturn(checkoutCommand).when(repository).checkout();
    }

    private static void mockPull(Git repository) {
        var pullCommand = mock(PullCommand.class);
        doReturn(pullCommand).when(pullCommand).setTransportConfigCallback(any(TransportConfigCallback.class));

        doReturn(pullCommand).when(repository).pull();
    }

    private static void mockAdd(Git repository) {
        var addCommand = mock(AddCommand.class);
        doReturn(addCommand).when(addCommand).addFilepattern(anyString());

        doReturn(addCommand).when(repository).add();
    }

    private static void mockCommit(Git repository) {
        var commitCommand = mock(CommitCommand.class);
        doReturn(commitCommand).when(commitCommand).setMessage(anyString());
        doReturn(commitCommand).when(commitCommand).setAuthor(anyString(), anyString());

        doReturn(commitCommand).when(repository).commit();
    }

    private static void mockPush(Git repository) {
        var pushCommand = mock(PushCommand.class);
        doReturn(pushCommand).when(pushCommand).setTransportConfigCallback(any(TransportConfigCallback.class));

        doReturn(pushCommand).when(repository).push();
    }

    private static void mockPushException(Git repository) throws GitAPIException {
        var pushCommand = mock(PushCommand.class);
        doReturn(pushCommand).when(pushCommand).setTransportConfigCallback(any(TransportConfigCallback.class));
        doThrow(InvalidRemoteException.class).when(pushCommand).call();

        doReturn(pushCommand).when(repository).push();
    }
}
