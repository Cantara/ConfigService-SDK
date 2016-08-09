package no.cantara.cs.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.cantara.cs.dto.CheckForUpdateRequest;
import no.cantara.cs.dto.ClientConfig;
import no.cantara.cs.dto.ClientRegistrationRequest;
import no.cantara.cs.dto.event.EventExtractionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 2015-07-13.
 */
public class ConfigServiceClient {
    public static final Charset CHARSET = Charset.forName("UTF-8");
    public static final String CLIENT_ID = "clientId";
    public static final String LAST_CHANGED = "lastChanged";
    public static final String COMMAND = "command";
    public static final String EVENT_EXTRACTION_CONFIGS = "eventExtractionConfigs";

    public static final int DEFAULT_TIMEOUT_MILLIS = 30_000;
    private static final String DEFAULT_APPLICATION_STATE_FILENAME = "applicationState.properties";
    private static final Logger log = LoggerFactory.getLogger(ConfigServiceClient.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final String url;
    private final String username;
    private final String password;
    private String applicationStateFilename;
    private int timeoutMillis;


    public ConfigServiceClient(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.applicationStateFilename = DEFAULT_APPLICATION_STATE_FILENAME;
        this.timeoutMillis = DEFAULT_TIMEOUT_MILLIS;
    }

    public ConfigServiceClient withApplicationStateFilename(String applicationStateFilename) {
        this.applicationStateFilename = applicationStateFilename;
        return this;
    }

    public ConfigServiceClient withTimeout(int timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        return this;
    }

    public ClientConfig registerClient(ClientRegistrationRequest request) throws IOException, HttpException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url + "/registration").openConnection();
        connection.setConnectTimeout(timeoutMillis);
        connection.setReadTimeout(timeoutMillis);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        if (username != null && password != null) {
            String usernameAndPassword = username + ":" + password;
            String encoded = Base64.getEncoder().encodeToString(usernameAndPassword.getBytes());
            connection.setRequestProperty("Authorization", "Basic " + encoded);
        }

        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        String jsonRequest = mapper.writeValueAsString(request);
        try (OutputStream output = connection.getOutputStream()) {
            output.write(jsonRequest.getBytes(CHARSET));
        }

        int responseCode = connection.getResponseCode();
        String responseMessage = connection.getResponseMessage();

        if (responseCode != HttpURLConnection.HTTP_OK) {
            log.warn("RegisterClient failed. url={}, responseCode={}, responseMessage={}", url, responseCode, responseMessage);
            throw new HttpException(responseCode, responseMessage);
        }

        try (Reader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
            StringBuilder result = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                result.append((char) c);
            }
            String jsonResponse = result.toString();
            ClientConfig clientConfig = mapper.readValue(jsonResponse, ClientConfig.class);
            log.info("registerClient ok. clientId={}", clientConfig.clientId);
            return clientConfig;
        }
    }


    public void saveApplicationState(ClientConfig clientConfig) {
        final Properties applicationState = new Properties();
        applicationState.put(CLIENT_ID, clientConfig.clientId);
        applicationState.put(LAST_CHANGED, clientConfig.config.getLastChanged());
        if (clientConfig.config.getStartServiceScript() != null) {
            applicationState.put(COMMAND, clientConfig.config.getStartServiceScript());
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonEventExtractionTags = mapper.writeValueAsString(clientConfig.config
                    .getEventExtractionConfigs());
            applicationState.put(EVENT_EXTRACTION_CONFIGS, jsonEventExtractionTags);
        } catch (JsonProcessingException io) {
            throw new RuntimeException(io);
        }
        OutputStream output = null;
        try {
            output = new FileOutputStream(applicationStateFilename);
            // save properties to project root folder
            applicationState.store(output, null);
        } catch (IOException io) {
            throw new RuntimeException(io);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    //intentionally ignored
                }
            }
        }
    }
    public Properties getApplicationState() {
        if (!new File(applicationStateFilename).exists()) {
            return null;
        }

        Properties properties = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream(applicationStateFilename);
            properties.load(input);
            return properties;
        } catch (IOException io) {
            throw new RuntimeException(io);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    //intentionally ignored
                }
            }
        }
    }

    public List<EventExtractionConfig> getEventExtractionConfigs() {
        String eventExtractionConfigs = getApplicationState().getProperty(EVENT_EXTRACTION_CONFIGS);
        if (eventExtractionConfigs == null) {
            return new ArrayList<>();
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(eventExtractionConfigs, new TypeReference<List<EventExtractionConfig>>(){});
        } catch (IOException io) {
            throw new RuntimeException(io);
        }
    }

    public void cleanApplicationState() {
        File applicationStatefile = new File(applicationStateFilename);
        if (applicationStatefile.exists()) {
            applicationStatefile.delete();
        }
    }


    public ClientConfig checkForUpdate(String clientId, CheckForUpdateRequest checkForUpdateRequest) throws IOException, HttpException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url + "/" + clientId + "/sync").openConnection();
        connection.setConnectTimeout(timeoutMillis);
        connection.setReadTimeout(timeoutMillis);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        if (username != null && password != null) {
            String usernameAndPassword = username + ":" + password;
            String encoded = Base64.getEncoder().encodeToString(usernameAndPassword.getBytes());
            connection.setRequestProperty("Authorization", "Basic " + encoded);
        }

        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        String jsonRequest = mapper.writeValueAsString(checkForUpdateRequest);
        try (OutputStream output = connection.getOutputStream()) {
            output.write(jsonRequest.getBytes(CHARSET));
        }

        String responseMessage = connection.getResponseMessage();
        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
            return null;
        }

        if (responseCode != HttpURLConnection.HTTP_OK) {
            log.warn("CheckForUpdate failed. url={}, responseCode={}, responseMessage={}",
                    url, responseCode, responseMessage);
            throw new HttpException(responseCode, responseMessage);
        }

        try (Reader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
            StringBuilder result = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                result.append((char) c);
            }
            String jsonResponse = result.toString();
            return mapper.readValue(jsonResponse, ClientConfig.class);
        }
    }

    public String getUrl() {
        return url;
    }

    public String getApplicationStateFilename() {
        return applicationStateFilename;
    }
}
