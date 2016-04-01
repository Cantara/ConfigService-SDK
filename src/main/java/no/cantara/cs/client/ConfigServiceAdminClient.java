package no.cantara.cs.client;

import no.cantara.cs.dto.Application;
import no.cantara.cs.dto.ApplicationConfig;
import no.cantara.cs.dto.ApplicationStatus;
import no.cantara.cs.dto.Client;
import no.cantara.cs.dto.ClientEnvironment;
import no.cantara.cs.dto.ClientStatus;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriTemplateHandler;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Client for admin users of ConfigService.
 *
 * @author <a href="mailto:asbjornwillersrud@gmail.com">Asbjørn Willersrud</a> 31/03/2016.
 */
public class ConfigServiceAdminClient {

    private static final String CLIENT_PATH = "/client";
    private static final String APPLICATION_PATH = "/application";
    private static final String CONFIG_PATH = APPLICATION_PATH + "/{applicationId}/config";

    private final HttpHeaders basicAuthHeaders;
    private final RestTemplate restTemplate = new RestTemplate();

    public ConfigServiceAdminClient(String baseUrl, String username, String password) {

        String plainCreds = username + ":" + password;
        byte[] plainCredsBytes = plainCreds.getBytes();
        
        byte[] base64CredsBytes = Base64.getEncoder().encode(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        basicAuthHeaders = new HttpHeaders();
        basicAuthHeaders.add("Authorization", "Basic " + base64Creds);

        DefaultUriTemplateHandler defaultUriTemplateHandler = new DefaultUriTemplateHandler();
        defaultUriTemplateHandler.setBaseUrl(baseUrl);
        restTemplate.setUriTemplateHandler(defaultUriTemplateHandler);
    }

    public Application registerApplication(String artifactId) throws IOException {
        Application application = new Application(artifactId);

        return restTemplate.exchange(APPLICATION_PATH,
                HttpMethod.POST, new HttpEntity<>(application, basicAuthHeaders), Application.class).getBody();
    }

    public ApplicationConfig createApplicationConfig(Application application, ApplicationConfig config) throws IOException {
        return restTemplate.exchange(CONFIG_PATH, HttpMethod.POST, new HttpEntity<>(config, basicAuthHeaders),
                ApplicationConfig.class, application.id).getBody();
    }

    public ApplicationConfig updateConfig(String applicationId, ApplicationConfig config) throws IOException {
        return restTemplate.exchange(CONFIG_PATH + "/{configId}", HttpMethod.PUT,
                new HttpEntity<>(config, basicAuthHeaders), ApplicationConfig.class, applicationId, config.getId()).getBody();
    }

    public Client getClient(String clientId) throws IOException {
        return restTemplate.exchange( CLIENT_PATH + "/{clientId}", HttpMethod.GET, new HttpEntity<>(null, basicAuthHeaders),
                Client.class, clientId).getBody();
    }

    public List<Client> getAllClients() throws IOException {
        return restTemplate.exchange(CLIENT_PATH, HttpMethod.GET, new HttpEntity<>(null, basicAuthHeaders),
                new ParameterizedTypeReference<List<Client>>() {}).getBody();
    }

    public Client putClient(Client client) throws IOException {
        return restTemplate.exchange(CLIENT_PATH + "/{clientId}", HttpMethod.PUT, new HttpEntity<>(client, basicAuthHeaders),
                Client.class, client.clientId).getBody();
    }

    public ClientStatus getClientStatus(String clientId) throws IOException {
        return restTemplate.exchange(CLIENT_PATH + "/{clientId}/status",
                HttpMethod.GET, new HttpEntity<>(null, basicAuthHeaders), ClientStatus.class, clientId).getBody();
    }

    public ClientEnvironment getClientEnvironment(String clientId) throws IOException {
        return restTemplate.exchange(CLIENT_PATH + "/{clientId}/env",
                HttpMethod.GET, new HttpEntity<>(null, basicAuthHeaders), ClientEnvironment.class, clientId).getBody();
    }

    public ApplicationStatus getApplicationStatus(String artifactId) throws IOException {
        return restTemplate.exchange(APPLICATION_PATH + "/{artifactId}/status",
                HttpMethod.GET, new HttpEntity<>(null, basicAuthHeaders), ApplicationStatus.class, artifactId).getBody();
    }

    public List<Application> getAllApplications() throws IOException {
        return restTemplate.exchange(APPLICATION_PATH,
                HttpMethod.GET, new HttpEntity<>(null, basicAuthHeaders),
                new ParameterizedTypeReference<List<Application>>() {}).getBody();
    }

    public Map<String, ApplicationConfig> getAllConfigs() throws IOException {
        return restTemplate.exchange(CONFIG_PATH,
                HttpMethod.GET, new HttpEntity<>(null, basicAuthHeaders),
                new ParameterizedTypeReference<Map<String, ApplicationConfig>>() {},
                "applicationId-is-not-used-by-the-server").getBody();
    }

}
