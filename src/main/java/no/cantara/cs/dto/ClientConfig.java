package no.cantara.cs.dto;

/**
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 2015-08-23.
 */
public class ClientConfig {
    public String clientId;
    public String clientSecret;
    public ApplicationConfig config;

    private ClientConfig() {}   //for jackson

    public ClientConfig(String clientId, ApplicationConfig config) {
        this.clientId = clientId;
        this.config = config;
    }
    public ClientConfig(String clientId, String clientSecret, ApplicationConfig config) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.config = config;
    }

    @Override
    public String toString() {
        return "ClientConfig{" +
                "clientId='" + clientId + '\'' +
                ", clientSecret='" + clientSecret + '\'' +
                ", " + config +
                '}';
    }
}
