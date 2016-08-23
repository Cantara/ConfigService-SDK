package no.cantara.cs.util;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.cantara.cs.client.ConfigServiceAdminClient;
import no.cantara.cs.dto.Application;
import no.cantara.cs.dto.ApplicationConfig;
import no.cantara.cs.dto.Client;

/**
 * Creates a new configuration for an existing application, and links it to a given client ID.
 * <p>
 * NOTE! To update an existing config, run {@link UpdateConfig} instead!!!
 *
 * @author Sindre Mehus
 */
public class RegisterConfig {

    private static final Logger log = LoggerFactory.getLogger(RegisterConfig.class);

    private final ConfigServiceAdminClient adminClient;

    public RegisterConfig(ConfigServiceAdminClient adminClient) {
        this.adminClient = adminClient;
    }

    public RegisterConfig(Environment environment) {
        this(new ConfigServiceAdminClient(environment));
    }

    /*
     * Expects an existing Application.
     * Create new ApplicationConfig and register new Client which use this new ApplicationConfig.
     *
     * https://wiki.cantara.no/display/JAU/Create+Application+and+ApplicationConfig
     * https://wiki.cantara.no/display/JAU/Preregister+Client+with+specific+ApplicationConfig
     */
    public void registerConfig(Path applicationFilename, Path applicationConfigFilename, String clientId) throws Exception {

        Application application = JsonUtil.readApplicationFromFile(applicationFilename);
        ApplicationConfig applicationConfig = JsonUtil.readConfigFromFile(applicationConfigFilename);
        log.info("Create new ApplicationConfig for {}: \n {}", application, JsonUtil.toJson(applicationConfig));

        applicationConfig = adminClient.createApplicationConfig(application, applicationConfig);

        log.info("Linking clientId={} to ApplicationConfig, applicationConfigId={}", clientId, applicationConfig.getId());
        adminClient.putClient(new Client(clientId, applicationConfig.getId(), true));
    }
}
