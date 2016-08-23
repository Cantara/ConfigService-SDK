package no.cantara.cs.util;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.cantara.cs.client.ConfigServiceAdminClient;
import no.cantara.cs.dto.Application;
import no.cantara.cs.dto.ApplicationConfig;

/**
 * Deletes an application config.
 *
 * @author Sindre Mehus
 */
public class DeleteConfig {

    private static final Logger log = LoggerFactory.getLogger(DeleteConfig.class);

    private final ConfigServiceAdminClient adminClient;

    public DeleteConfig(ConfigServiceAdminClient adminClient) {
        this.adminClient = adminClient;
    }

    public DeleteConfig(Environment environment) {
        this(new ConfigServiceAdminClient(environment));
    }

    public void deleteConfig(Path applicationFile, Path configFile) throws Exception {
        Application application = JsonUtil.readApplicationFromFile(applicationFile);
        ApplicationConfig config = JsonUtil.readConfigFromFile(configFile);

        adminClient.deleteConfig(application.id, config);
        log.info("Deleted config for application {}", application.toString());
    }
}
