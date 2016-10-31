package no.cantara.cs.util;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.cantara.cs.client.ConfigServiceAdminClient;
import no.cantara.cs.dto.Application;
import no.cantara.cs.dto.ApplicationConfig;

/**
 * Registers a new Application and optionally an ApplicationConfig for that application.
 *
 * @author <a href="mailto:asbjornwillersrud@gmail.com">Asbjørn Willersrud</a> 01/04/2016.
 */
public class RegisterApplication {

    private static final Logger log = LoggerFactory.getLogger(RegisterApplication.class);

    private final ConfigServiceAdminClient adminClient;

    public RegisterApplication(ConfigServiceAdminClient adminClient) {
        this.adminClient = adminClient;
    }

    public RegisterApplication(Environment environment) {
        this(new ConfigServiceAdminClient(environment));
    }

    public void registerApplication(String artifactId) throws Exception {
        Application application = adminClient.registerApplication(artifactId);
        log.info("Registered application {}", JsonUtil.toJson(application));
    }

    public void registerApplication(String artifactId, Path configFile) throws Exception {
        Application application = adminClient.registerApplication(artifactId);
        log.info("Registered application {}", JsonUtil.toJson(application));

        ApplicationConfig applicationConfigInput = JsonUtil.readConfigFromFile(configFile);
        ApplicationConfig applicationConfigResult = adminClient.createApplicationConfig(application, applicationConfigInput);
        log.info("Registered applicationconfig {}", JsonUtil.toJson(applicationConfigResult));
    }
}
