package no.cantara.cs.client;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.cantara.cs.dto.Client;
import no.cantara.cs.dto.*;
import no.cantara.cs.util.Environment;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

    private final WebTarget applicationResource;
    private final WebTarget clientResource;
    private final ObjectMapper mapper;

    public ConfigServiceAdminClient(String baseUrl, String username, String password) {
        mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        javax.ws.rs.client.Client restClient = ClientBuilder.newClient()
                .register(new Authenticator(username, password));

        applicationResource = restClient.target(baseUrl).path(APPLICATION_PATH);
        clientResource = restClient.target(baseUrl).path(CLIENT_PATH);
    }

    public ConfigServiceAdminClient(Environment environment) {
        this(environment.getUrl(), environment.getUsername(), environment.getPassword());
    }

    public Application registerApplication(String artifactId) throws IOException {
        Application application = new Application(artifactId);
        Response response = applicationResource.request()
                .accept(APPLICATION_JSON_TYPE)
                .post(jsonEntity(application));
        return readValue(response, Application.class);
    }

    public void deleteApplication(String applicationId) throws IOException {
        applicationResource.path(applicationId).request().delete();
    }

    public ApplicationConfig createApplicationConfig(Application application, ApplicationConfig config) throws IOException {
        Response response = applicationResource.path(application.id + "/config/").request().accept(new MediaType[]{MediaType.APPLICATION_JSON_TYPE}).post(jsonEntity(config));
        return readValue(response, ApplicationConfig.class);
    }

    public ApplicationConfig updateConfig(String applicationId, ApplicationConfig config) throws IOException {
        Response response = applicationResource.path(applicationId + "/config/" + config.getId()).request().accept(new MediaType[]{MediaType.APPLICATION_JSON_TYPE}).put(jsonEntity(config));
        return readValue(response, ApplicationConfig.class);
    }

    public void deleteConfig(String applicationId, ApplicationConfig config) throws IOException {
        applicationResource.path(applicationId + "/config/" + config.getId()).request().accept(new MediaType[]{MediaType.APPLICATION_JSON_TYPE}).delete();
    }

    public Client getClient(String clientId) throws IOException {
        Response response = clientResource.path(clientId).request().get();
        return readValue(response, Client.class);
    }

    public List<Client> getAllClients() throws IOException {
        return readValue(clientResource.request().get(), new TypeReference<List<Client>>(){});
    }

    public Client putClient(Client client) throws IOException {
        Response response = clientResource.path(client.clientId).request().put(jsonEntity(client));
        return readValue(response, Client.class);
    }

    public ClientStatus getClientStatus(String clientId) throws IOException {
        Response response = clientResource.path(clientId + "/status").request().accept(
            APPLICATION_JSON_TYPE).get();
        return readValue(response, ClientStatus.class);
    }

    public ClientEnvironment getClientEnvironment(String clientId) throws IOException {
        Response response = clientResource.path(clientId + "/env").request().accept(
            APPLICATION_JSON_TYPE).get();
        return readValue(response, ClientEnvironment.class);
    }

    public ApplicationStatus getApplicationStatus(String artifactId) throws IOException {
        Response response = applicationResource.path(artifactId + "/status").request().accept(
            APPLICATION_JSON_TYPE).get();
        return readValue(response, ApplicationStatus.class);
    }

    public ApplicationConfig getApplicationConfig(String applicationId) throws IOException {
        Response response = applicationResource.path(applicationId + "/config").request().accept(
            APPLICATION_JSON_TYPE).get();
        return readValue(response, ApplicationConfig.class);
    }

    public ApplicationConfig getApplicationEvents(String applicationId) throws IOException {
        Response response = applicationResource.path(applicationId + "/events").request().accept(
            APPLICATION_JSON_TYPE).get();
        return readValue(response, ApplicationConfig.class);
    }


    public List<Application> getAllApplications() throws IOException {
        Response response = applicationResource.request().accept(new MediaType[]{MediaType.APPLICATION_JSON_TYPE}).get();
        return readValue(response, new TypeReference<List<Application>>() {
        });
    }


    public List<ApplicationConfig> getAllConfigs() throws IOException {
        final Response response = applicationResource.path("/config").request().get();
        return readValue(response, new TypeReference<List<ApplicationConfig>>() {
        });
    }

    private Entity<String> jsonEntity(Object object) throws JsonProcessingException {
        return Entity.json(mapper.writeValueAsString(object));
    }

    private <T> T readValue(Response response, Class<T> clazz) throws IOException {
        return mapper.readValue(response.readEntity(String.class), clazz);
    }

    private <T> T readValue(Response response, TypeReference<T> type) throws IOException {
        return mapper.readValue(response.readEntity(String.class), type);
    }

    private class Authenticator implements ClientRequestFilter {

        private final String user;
        private final String password;

        Authenticator(String user, String password) {
            this.user = user;
            this.password = password;
        }

        public void filter(ClientRequestContext requestContext) throws IOException {
            MultivaluedMap<String, Object> headers = requestContext.getHeaders();
            final String basicAuthentication = getBasicAuthentication();
            headers.add("Authorization", basicAuthentication);
        }

        private String getBasicAuthentication() {
            String token = this.user + ":" + this.password;
            try {
                return "BASIC " + DatatypeConverter.printBase64Binary(token.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                throw new IllegalStateException("Cannot encode with UTF-8", ex);
            }
        }
    }
}
