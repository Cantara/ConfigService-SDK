package no.cantara.cs.dto;

import java.io.Serializable;

public class ClientStatus implements Serializable {

    public Client client;
    public ClientHeartbeatData latestClientHeartbeatData;

    private ClientStatus() {
        // For Jackson
    }

    public ClientStatus(Client client, ClientHeartbeatData latestClientHeartbeatData) {
        this.client = client;
        this.latestClientHeartbeatData = latestClientHeartbeatData;
    }
}
