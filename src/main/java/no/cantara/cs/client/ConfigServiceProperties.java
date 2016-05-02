package no.cantara.cs.client;

import java.io.IOException;
import java.util.Properties;

public class ConfigServiceProperties {
    static final String CONFIG_SERVICE_URL_KEY = "configservice.url";
    static final String CONFIG_SERVICE_USERNAME_KEY = "configservice.username";
    static final String CONFIG_SERVICE_PASSWORD_KEY = "configservice.password";
    static final String CONFIG_SERVICE_ARTIFACT_ID = "configservice.artifactid";
    static final String CONFIG_SERVICE_CLIENT_ID = "configservice.clientid";
    static final String CONFIG_SERVICE_CONFIGURATION_STORE_DIRECTORY = "configservice.configuration.store.directory";
    static final String CONFIG_SERVICE_DOWNLOAD_ITEM_DIRECTORY = "configservice.download.item.directory";
    static final String CONFIG_SERVICE_ALLOW_FALLBACK_TO_LOCAL_CONFIG = "configservice.allow.fallback.to.local.config";

    private final Properties propertiesFromFile;

    public ConfigServiceProperties(String propertiesFilename) {
        this.propertiesFromFile = loadPropertiesFromFile(propertiesFilename);
    }

    public ConfigServiceClient buildClient() {
        return new ConfigServiceClient(getServiceConfigUrl(), getUsername(), getPassword());
    }

    public ApplicationConfigurator buildApplicationConfigurator() {
        return new ApplicationConfigurator(buildClient())
                .setArtifactId(getArtifactId())
                .setClientId(getClientId())
                .setConfigurationStoreDirectory(getConfigurationStoreDirectory())
                .setDownloadItemDirectory(getDownloadItemDirectory())
                .setAllowFallbackToLocalConfiguration(isAllowFallbackToLocalConfig());
    }

    public String getServiceConfigUrl() {
        return getStringFromEnvOrPropertiesFile(CONFIG_SERVICE_URL_KEY);
    }
    public String getUsername() {
        return getStringFromEnvOrPropertiesFile(CONFIG_SERVICE_USERNAME_KEY);
    }
    public String getPassword() {
        return getStringFromEnvOrPropertiesFile(CONFIG_SERVICE_PASSWORD_KEY);
    }
    public String getArtifactId() {
        return getStringFromEnvOrPropertiesFile(CONFIG_SERVICE_ARTIFACT_ID);
    }
    public String getClientId() {
        return getStringFromEnvOrPropertiesFile(CONFIG_SERVICE_CLIENT_ID);
    }
    public String getConfigurationStoreDirectory() {
        return getStringFromEnvOrPropertiesFile(CONFIG_SERVICE_CONFIGURATION_STORE_DIRECTORY);
    }
    public String getDownloadItemDirectory() {
        return getStringFromEnvOrPropertiesFile(CONFIG_SERVICE_DOWNLOAD_ITEM_DIRECTORY);
    }
    public Boolean isAllowFallbackToLocalConfig() {
        return  Boolean.valueOf(getStringFromEnvOrPropertiesFile(CONFIG_SERVICE_ALLOW_FALLBACK_TO_LOCAL_CONFIG));

    }

    private String getStringFromEnvOrPropertiesFile(String key) {
        String envVariable = System.getenv(key);
        if (envVariable != null && !envVariable.isEmpty()) {
            return envVariable;
        }
        return propertiesFromFile.getProperty(key);
    }

    private Properties loadPropertiesFromFile(String filename) {
        if (filename == null || filename.isEmpty()) {
            return new Properties();
        }

        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }
}