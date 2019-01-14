package no.cantara.cs.dto;

import java.io.Serializable;

public class ClientAlias implements Serializable {

    public String clientId;
    public String clientName;

    private ClientAlias() {
        // For Jackson
    }

    public ClientAlias(String clientId, String clientName) {
        this.clientId = clientId;
        this.clientName = clientName;
    }
}
