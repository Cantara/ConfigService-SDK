package no.cantara.cs.dto;

import java.io.Serializable;
import java.time.Instant;

public class ClientHeartbeatData implements Serializable {

    public String artifactId;
    public String tags;
    public String clientName;

    public String configLastChanged;
    public String applicationConfigId;
    public String timeOfContact;

    private ClientHeartbeatData() {
        // For Jackson
    }

    public ClientHeartbeatData(ClientRegistrationRequest registration, ApplicationConfig config) {
        artifactId = registration.artifactId;
        tags = registration.tags;
        clientName = registration.clientName;
        configLastChanged = config.getLastChanged();
        applicationConfigId = config.getId();
        timeOfContact = Instant.now().toString();
    }

    public ClientHeartbeatData(CheckForUpdateRequest checkForUpdateRequest, ApplicationConfig config, String artifactId) {
        this.artifactId = artifactId;
        tags = checkForUpdateRequest.tags;
        clientName = checkForUpdateRequest.clientName;
        configLastChanged = config.getLastChanged();
        applicationConfigId = config.getId();
        timeOfContact = Instant.now().toString();
    }

}
