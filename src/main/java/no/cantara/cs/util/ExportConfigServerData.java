package no.cantara.cs.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.cantara.cs.client.ConfigServiceAdminClient;
import no.cantara.cs.dto.Application;
import no.cantara.cs.dto.ApplicationConfig;
import no.cantara.cs.dto.Client;

/**
 * Export all applications, applicationconfigs and clients from a given ConfigServer instance. Persist as json files.
 *
 * @author <a href="mailto:asbjornwillersrud@gmail.com">Asbjørn Willersrud</a> 01/04/2016.
 */
public class ExportConfigServerData {

    private static final Logger log = LoggerFactory.getLogger(ExportConfigServerData.class);

    private final ConfigServiceAdminClient adminClient;

    public ExportConfigServerData(ConfigServiceAdminClient adminClient) {
        this.adminClient = adminClient;
    }

    public ExportConfigServerData(Environment environment) {
        this(new ConfigServiceAdminClient(environment));
    }

    public void export(Path targetDirectory) throws Exception {

        List<Application> applications = adminClient.getAllApplications();
        List<ApplicationConfig> configs = adminClient.getAllConfigs();
        List<Client> clients = adminClient.getAllClients();

        applications.forEach(application -> writeToFile(targetDirectory.resolve(application.artifactId).resolve(application.artifactId + ".json"), application));

        configs.forEach(config -> {
            final String configIdentifier = config.getName() + "-" + config.getId();
            writeToFile(targetDirectory.resolve("config/" + configIdentifier).resolve(configIdentifier + ".json"), config);
            clients.stream()
                   .filter(client -> config.getId().equals(client.applicationConfigId))
                   .forEach(client -> writeToFile(targetDirectory.resolve("config/" + configIdentifier + "/client/" + client.clientId + ".json"), client));
        });
    }

    private void writeToFile(Path path, Object object) {
        try {
            Path parent = path.getParent();
            if (!Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            log.info("Writing file {}", path);
            Files.write(path, new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsBytes(object));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
