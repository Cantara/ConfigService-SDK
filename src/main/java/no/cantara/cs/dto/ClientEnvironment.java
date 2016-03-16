package no.cantara.cs.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

public class ClientEnvironment implements Serializable {

    public Map<String, String> envInfo;
    public String timestamp;

    private ClientEnvironment() {
        // For Jackson
    }

    public ClientEnvironment(Map<String, String> envInfo) {
        this.envInfo = envInfo;
        timestamp = Instant.now().toString();
    }
}
