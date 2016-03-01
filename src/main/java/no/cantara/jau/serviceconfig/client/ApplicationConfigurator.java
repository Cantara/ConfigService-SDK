package no.cantara.jau.serviceconfig.client;

import no.cantara.jau.serviceconfig.dto.CheckForUpdateRequest;
import no.cantara.jau.serviceconfig.dto.ClientConfig;
import no.cantara.jau.serviceconfig.dto.ClientRegistrationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class ApplicationConfigurator {

    private static final Logger log = LoggerFactory.getLogger(ApplicationConfigurator.class);

    private ConfigServiceClient configServiceClient;
    private boolean allowFallbackToLocalConfiguration = true;

    public ApplicationConfigurator(ConfigServiceClient configServiceClient) {
        this.configServiceClient = configServiceClient;
    }

    public ApplicationConfigurator setAllowFallbackToLocalConfiguration(boolean allowFallbackToLocalConfiguration) {
        this.allowFallbackToLocalConfiguration = allowFallbackToLocalConfiguration;
        return this;
    }

    public void configureApplication(String artifactId, String configurationStoreDirectory) throws IOException {
        Properties applicationState = configServiceClient.getApplicationState();

        ClientConfig clientConfig = null;
        if (applicationState != null) {
            try {
                String clientId = applicationState.getProperty(ConfigServiceClient.CLIENT_ID);
                log.info("Found persisted application state with clientId: {}. Checking for updated clientconfig", clientId);
                clientConfig = configServiceClient.checkForUpdate(clientId,
                        new CheckForUpdateRequest(applicationState.getProperty(ConfigServiceClient.LAST_CHANGED)));
                if (clientConfig != null) {
                    log.info("Found updated clientconfig, id={}, lastChanged={}", clientConfig.serviceConfig.getId(), clientConfig.serviceConfig.getLastChanged());
                } else {
                    log.info("No new clientconfig, using existing");
                }

            } catch (Exception e) {
                log.error("checkForUpdate failed - falling back to existing clientconfig", e);
            }
        } else {
            log.info("No previous application state persisted, registering new configservice client, artifactId={}", artifactId);
            try {
                clientConfig = configServiceClient.registerClient(new ClientRegistrationRequest(artifactId));
            } catch (Exception e) {
                if (allowFallbackToLocalConfiguration) {
                    log.error("registerClient failed - falling back to local configuration files", e);
                } else {
                    throw e;
                }
            }
        }
        if (clientConfig != null) {
            configServiceClient.saveApplicationState(clientConfig);
            ConfigurationStoreUtil.toFiles(clientConfig.serviceConfig.getConfigurationStores(), configurationStoreDirectory);
        }
    }

}
