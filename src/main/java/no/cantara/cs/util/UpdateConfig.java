package no.cantara.cs.util;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.cantara.cs.client.ConfigServiceAdminClient;
import no.cantara.cs.dto.Application;
import no.cantara.cs.dto.ApplicationConfig;

/**
 * Updates an existing application config.
 * <p>
 * NOTE! Before running this utility, make sure to run {@link ExportConfigServerData} to get the latest config.
 *
 * @author <a href="mailto:asbjornwillersrud@gmail.com">Asbjørn Willersrud</a> 01/04/2016.
 */
public class UpdateConfig {

    private static final Logger log = LoggerFactory.getLogger(UpdateConfig.class);

    private final ConfigServiceAdminClient adminClient;

    public UpdateConfig(ConfigServiceAdminClient adminClient) {
        this.adminClient = adminClient;
    }

    public UpdateConfig(Environment environment) {
        this(new ConfigServiceAdminClient(environment));
    }

    public void updateConfig(Path applicationConfigFile, Path applicationFile) throws Exception {
        Application application = JsonUtil.readApplicationFromFile(applicationFile);
        log.info("Updating config for application {}", application.toString());

        ApplicationConfig config = JsonUtil.readConfigFromFile(applicationConfigFile);
        config.setUpdated();
        log.info("New config {}", config.toString());

        adminClient.updateConfig(application.id, config);
    }
}
