package no.cantara.cs.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.cantara.cs.client.ConfigServiceAdminClient;
import no.cantara.cs.dto.Application;
import no.cantara.cs.dto.ApplicationConfig;
import no.cantara.cs.dto.Client;

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

    private List<Application> applications = new ArrayList<>();
    private Map<String, List<ApplicationConfig>> applicationConfigs = new LinkedHashMap<>();
    private Map<String, List<Client>> clients = new LinkedHashMap<>();

    private ConfigServiceAdminClient adminClient;

    public ImportConfigServerData(ConfigServiceAdminClient adminClient) {
        this.adminClient = adminClient;
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
            System.out.println("POST application/: " + toJson(application));
            Application registeredApplication = adminClient.registerApplication(application.artifactId);
            for (ApplicationConfig applicationConfig : applicationConfigs.get(application.artifactId)) {
                System.out.println("POST application/" + application.id + "/config: " + applicationConfig.getName());
                ApplicationConfig registeredApplicationConfig = adminClient.createApplicationConfig(registeredApplication, applicationConfig);
                if (clients.containsKey(applicationConfig.getId())) {
                    for (Client client : clients.get(applicationConfig.getId())) {
                        client.applicationConfigId = registeredApplicationConfig.getId();
                        System.out.println("PUT client/" + client.clientId + ": " + toJson(client));
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

    public static <T> T readFromPath(Path path, Class<T> clazz) {
        try {
            return new ObjectMapper().readValue(Files.readAllBytes(path), clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toJson(Object object) {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
