package no.cantara.cs.dto;

import java.io.Serializable;
import java.util.UUID;

public class Client implements Serializable {
    public String clientId;
    public String clientSecret;
    public String applicationConfigId;
    public boolean autoUpgrade = true;

    private Client() {} // for Jackson

    public Client(String clientId, String applicationConfigId, boolean autoUpgrade) {
        this.clientId = clientId;
        this.clientSecret = UUID.randomUUID().toString();
        this.applicationConfigId = applicationConfigId;
        this.autoUpgrade = autoUpgrade;
    }
}
