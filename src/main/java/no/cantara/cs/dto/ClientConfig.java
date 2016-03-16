package no.cantara.cs.dto;

/**
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 2015-08-23.
 */
public class ClientConfig {
    public String clientId;
    public ApplicationConfig config;

    //for jackson
    private ClientConfig() {
    }

    public ClientConfig(String clientId, ApplicationConfig config) {
        this.clientId = clientId;
        this.config = config;
    }

    @Override
    public String toString() {
        return "ClientConfig{" +
                "clientId='" + clientId + '\'' +
                ", " + config +
                '}';
    }
}
