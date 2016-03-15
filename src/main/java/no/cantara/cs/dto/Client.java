package no.cantara.cs.dto;

import java.io.Serializable;

public class Client implements Serializable {

    public String clientId;
    public String applicationConfigId;
    public boolean autoUpgrade = true;

    private Client() {
        // For Jackson
    }

    public Client(String clientId, String applicationConfigId, boolean autoUpgrade) {
        this.clientId = clientId;
        this.applicationConfigId = applicationConfigId;
        this.autoUpgrade = autoUpgrade;
    }
}
