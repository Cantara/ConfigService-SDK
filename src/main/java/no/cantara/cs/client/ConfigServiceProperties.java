package no.cantara.cs.client;

import java.io.IOException;
import java.util.Properties;

public class ConfigServiceProperties {

    private static final String CONFIG_SERVICE_URL_KEY = "configservice.url";
    private static final String CONFIG_SERVICE_ARTIFACT_ID = "configservice.artifactid";
    private static final String CONFIG_SERVICE_CLIENT_ID = "configservice.clientid";
    private static final String CONFIG_SERVICE_USERNAME_KEY = "configservice.username";
    private static final String CONFIG_SERVICE_PASSWORD_KEY = "configservice.password";
    private static final String CONFIG_SERVICE_CONFIGURATION_STORE_DIRECTORY = "configservice.configuration.store.directory";
    private static final String CONFIG_SERVICE_ALLOW_FALLBACK_TO_LOCAL_CONFIG = "configservice.allow.fallback.to.local.config";
    private String url;
    private String artifactId;

    private String clientId;
    private String username;
    private String password;
    private String configurationStoreDirectory;
    private Boolean allowFallbackToLocalConfig;

    public ConfigServiceProperties(String propertiesFilename) {
        Properties properties = loadPropertiesFromFile(propertiesFilename);
        url = properties.getProperty(CONFIG_SERVICE_URL_KEY);
        artifactId = properties.getProperty(CONFIG_SERVICE_ARTIFACT_ID);
        clientId = properties.getProperty(CONFIG_SERVICE_CLIENT_ID, null);
        username = properties.getProperty(CONFIG_SERVICE_USERNAME_KEY);
        password = properties.getProperty(CONFIG_SERVICE_PASSWORD_KEY);
        configurationStoreDirectory = properties.getProperty(CONFIG_SERVICE_CONFIGURATION_STORE_DIRECTORY);
        allowFallbackToLocalConfig = Boolean.valueOf(properties.getProperty(CONFIG_SERVICE_ALLOW_FALLBACK_TO_LOCAL_CONFIG));
    }

    public ConfigServiceClient buildClient() {
        return new ConfigServiceClient(url, username, password);
    }

    public ApplicationConfigurator buildApplicationConfigurator() {
        return new ApplicationConfigurator(buildClient())
                .setArtifactId(artifactId)
                .setClientId(clientId)
                .setConfigurationStoreDirectory(configurationStoreDirectory)
                .setAllowFallbackToLocalConfiguration(allowFallbackToLocalConfig);
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getServiceConfigUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getConfigurationStoreDirectory() {
        return configurationStoreDirectory;
    }

    private Properties loadPropertiesFromFile(String filename) {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

}
