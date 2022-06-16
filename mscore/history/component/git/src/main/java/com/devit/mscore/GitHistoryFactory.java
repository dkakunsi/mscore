package com.devit.mscore;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.ConfigException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;

public class GitHistoryFactory {

    private static final String DEFAULT_LOCAL_REPOSITORY = "./repo";

    private static final String GIT_URL = "services.%s.git.url";

    private static final String LOCAL_REPOSITORY = "services.%s.git.dir";

    private static final String PRIVATE_KEY = "services.%s.git.key";

    private static final String PASS_PHRASE = "services.%s.git.passPhrase";

    private static final String HOST_NAME = "services.%s.git.hostName";

    private static final String HOST_KEY = "services.%s.git.hostKey";

    private Configuration configuration;

    private Git repository;

    GitHistoryFactory(Configuration configuration, Git repository) {
        this.configuration = configuration;
        this.repository = repository;
    }

    public static GitHistoryFactory of(ApplicationContext context, Configuration configuration) throws ConfigException {
        try {
            var repoDir = getRepoDirectory(context, configuration);

            Git repository;
            if (isLocalRepositoryExists(repoDir)) {
                repository = Git.open(repoDir);
            } else {
                repoDir.mkdir();
                repository = cloneRepository(context, configuration, repoDir,
                        createTransportConfigCallback(context, configuration));
            }
            return new GitHistoryFactory(configuration, repository);
        } catch (Exception ex) {
            throw new ConfigException(ex);
        }
    }

    private static File getRepoDirectory(ApplicationContext context, Configuration configuration)
            throws ConfigException {
        var gitDir = getLocalDirectory(context, configuration);
        return Paths.get(gitDir).toFile();
    }

    private static String getLocalDirectory(ApplicationContext context, Configuration configuration) throws ConfigException {
        var dirKey = String.format(LOCAL_REPOSITORY, configuration.getServiceName());
        return configuration.getConfig(context, dirKey).orElse(DEFAULT_LOCAL_REPOSITORY);
    }

    static boolean isLocalRepositoryExists(File repoDir) {
        return Files.exists(Paths.get(repoDir.toString(), ".git"));
    }

    static Git cloneRepository(ApplicationContext context, Configuration configuration, File repoDir,
            TransportConfigCallback transportConfigCallback)
            throws ConfigException {

        var uriConfigName = String.format(GIT_URL, configuration.getServiceName());
        var uriConfig = configuration.getConfig(context, uriConfigName)
                .orElseThrow(() -> new ConfigException("No git uri is provided."));
        var localTransportConfigCallback = transportConfigCallback != null ? transportConfigCallback
                : createTransportConfigCallback(context, configuration);

        try {
            return Git.cloneRepository().setTransportConfigCallback(localTransportConfigCallback)
                    .setURI(uriConfig).setDirectory(repoDir).call();
        } catch (GitAPIException ex) {
            throw new ConfigException(ex);
        }
    }

    static TransportConfigCallback createTransportConfigCallback(ApplicationContext context,
            Configuration configuration) {
        return transport -> {
            try {
                var sshTransport = (SshTransport) transport;
                sshTransport.setSshSessionFactory(createSshSessionFactory(context, configuration));
            } catch (ConfigException ex) {
                throw new ApplicationRuntimeException(ex);
            }
        };
    }

    private static SshSessionFactory createSshSessionFactory(ApplicationContext context, Configuration configuration)
            throws ConfigException {
        var serviceName = configuration.getServiceName();
        var hostNameConfigName = String.format(HOST_NAME, serviceName);
        var hostKeyConfigName = String.format(HOST_KEY, serviceName);
        var privateKeyConfigName = String.format(PRIVATE_KEY, serviceName);
        var passPhraseConfigName = String.format(PASS_PHRASE, serviceName);

        var hostName = configuration.getConfig(context, hostNameConfigName)
                .orElseThrow(() -> new ConfigException("No host name configured."));
        var hostKey = configuration.getConfig(context, hostKeyConfigName)
                .orElseThrow(() -> new ConfigException("No host key configured."));
        var privateKey = configuration.getConfig(context, privateKeyConfigName)
                .orElseThrow(() -> new ConfigException("No private key configured."));
        var passphrase = configuration.getConfig(context, passPhraseConfigName)
                .orElseThrow(() -> new ConfigException("No passphrase provided."));

        return new CustomJschConfigSessionFactory(hostName, hostKey, privateKey, passphrase);
    }

    public GitHistory historyManager(ApplicationContext context) throws ConfigException {
        var gitDir = getLocalDirectory(context, this.configuration);
        return new GitHistory(gitDir, this.repository, createTransportConfigCallback(context, this.configuration));
    }

    Git getRepository() {
        return this.repository;
    }
}