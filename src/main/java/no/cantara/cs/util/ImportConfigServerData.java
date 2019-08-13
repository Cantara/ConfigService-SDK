package no.cantara.cs.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.cantara.cs.client.ConfigServiceAdminClient;
import no.cantara.cs.dto.Application;
import no.cantara.cs.dto.ApplicationConfig;
import no.cantara.cs.dto.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Asbjørn Willersrud
 */
public class ImportConfigServerData {

    private static final Logger log = LoggerFactory.getLogger(ImportConfigServerData.class);

    private final List<Application> applications = new ArrayList<>();
    private final Map<String, List<ApplicationConfig>> applicationConfigs = new LinkedHashMap<>();
    private final Map<String, List<Client>> clients = new LinkedHashMap<>();

    private final ConfigServiceAdminClient adminClient;

    public ImportConfigServerData(ConfigServiceAdminClient adminClient) {
        this.adminClient = adminClient;
    }

    public ImportConfigServerData(Environment environment) {
        this(new ConfigServiceAdminClient(environment));
    }

    public void importFiles(Path pathToDataFiles) throws Exception {

        Files.list(pathToDataFiles)
             .filter(Files::isDirectory)
             .forEach(applicationDirectory -> {
                 Path applicationFile = applicationDirectory.resolve(applicationDirectory.getFileName().toString() + ".json");
                 Application application = readFromPath(applicationFile, Application.class);
                 applications.add(application);

                 readConfigs(application, applicationDirectory);
             });

        ConfigServiceAdminClient adminClient = this.adminClient;

        for (Application application : applications) {
            log.info("POST application/: {}", toJson(application));
            Application registeredApplication = adminClient.registerApplication(application.artifactId);
            for (ApplicationConfig applicationConfig : applicationConfigs.get(application.artifactId)) {
                log.info("POST application/{}/config: {}", application.id, applicationConfig.getName());
                ApplicationConfig registeredApplicationConfig = adminClient.createApplicationConfig(registeredApplication, applicationConfig);
                if (clients.containsKey(applicationConfig.getId())) {
                    for (Client client : clients.get(applicationConfig.getId())) {
                        client.applicationConfigId = registeredApplicationConfig.getId();
                        log.info("PUT client/{}: {}", client.clientId, toJson(client));
                        adminClient.putClient(client);
                    }
                }
            }
        }
    }

    private void readConfigs(Application application, Path applicationDirectory) {
        try {
            Files.list(applicationDirectory)
                 .filter(Files::isDirectory)
                 .forEach(configDirectory -> {
                     Path configFile = configDirectory.resolve(configDirectory.getFileName().toString() + ".json");
                     ApplicationConfig config = readFromPath(configFile, ApplicationConfig.class);
                     if (!applicationConfigs.containsKey(application.artifactId)) {
                         applicationConfigs.put(application.artifactId, new ArrayList<>());
                     }
                     applicationConfigs.get(application.artifactId).add(config);

                     readClients(configDirectory);
                 });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void readClients(Path configDirectory) {
        try {
            Path clientDirectory = configDirectory.resolve("client");
            if (!clientDirectory.toFile().exists()) {
                return;
            }
            Files.list(clientDirectory)
                 .filter(Files::isRegularFile)
                 .forEach(clientFile -> {
                     Client client = readFromPath(clientFile, Client.class);
                     if (!clients.containsKey(client.applicationConfigId)) {
                         clients.put(client.applicationConfigId, new ArrayList<>());
                     }
                     clients.get(client.applicationConfigId).add(client);
                 });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T readFromPath(Path path, Class<T> clazz) {
        try {
            return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(Files.readAllBytes(path), clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toJson(Object object) {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
