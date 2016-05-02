package no.cantara.cs.client;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.cantara.cs.dto.CheckForUpdateRequest;
import no.cantara.cs.dto.ClientConfig;
import no.cantara.cs.dto.ClientRegistrationRequest;

public class ApplicationConfigurator {

    private static final Logger log = LoggerFactory.getLogger(ApplicationConfigurator.class);

    private ConfigServiceClient configServiceClient;
    private boolean allowFallbackToLocalConfiguration = true;
    private String artifactId;
    private String clientId;
    private String configurationStoreDirectory = ".";
    private String downloadItemDirectory = ".";

    public ApplicationConfigurator(ConfigServiceClient configServiceClient) {
        this.configServiceClient = configServiceClient;
    }

    public ApplicationConfigurator setAllowFallbackToLocalConfiguration(boolean allowFallbackToLocalConfiguration) {
        this.allowFallbackToLocalConfiguration = allowFallbackToLocalConfiguration;
        return this;
    }

    public ApplicationConfigurator setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    public ApplicationConfigurator setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public ApplicationConfigurator setConfigurationStoreDirectory(String configurationStoreDirectory) {
        this.configurationStoreDirectory = configurationStoreDirectory;
        return this;
    }

    public ApplicationConfigurator setDownloadItemDirectory(String downloadItemDirectory) {
        this.downloadItemDirectory = downloadItemDirectory;
        return this;
    }

    public void configureApplication() throws IOException {
        Properties applicationState = configServiceClient.getApplicationState();

        ClientConfig clientConfig = null;
        if (applicationState != null) {
            try {
                String clientId = applicationState.getProperty(ConfigServiceClient.CLIENT_ID);
                log.info("Found persisted application state with clientId: {}. Checking for updated clientconfig", clientId);
                clientConfig = configServiceClient.checkForUpdate(clientId,
                        new CheckForUpdateRequest(applicationState.getProperty(ConfigServiceClient.LAST_CHANGED)));
                if (clientConfig != null) {
                    log.info("Found updated clientconfig, id={}, lastChanged={}", clientConfig.config.getId(), clientConfig.config.getLastChanged());
                } else {
                    log.info("No new clientconfig, using existing");
                }

            } catch (Exception e) {
                log.error("checkForUpdate failed - falling back to existing clientconfig", e);
            }
        } else {
            log.info("No previous application state persisted, registering new configservice client, artifactId={}", artifactId);
            try {
                ClientRegistrationRequest request = new ClientRegistrationRequest(artifactId);
                request.clientId = clientId;
                clientConfig = configServiceClient.registerClient(request);
            } catch (Exception e) {
                if (allowFallbackToLocalConfiguration) {
                    log.error("registerClient failed - falling back to local configuration files", e);
                } else {
                    throw e;
                }
            }
        }
        if (clientConfig != null && configurationStoreDirectory != null) {
            configServiceClient.saveApplicationState(clientConfig);
            ConfigurationStoreUtil.toFiles(clientConfig.config.getConfigurationStores(), configurationStoreDirectory);
        }
        if (clientConfig != null && downloadItemDirectory != null) {
            DownloadUtil.downloadAllFiles(clientConfig.config.getDownloadItems(), downloadItemDirectory);
        }
    }

}
