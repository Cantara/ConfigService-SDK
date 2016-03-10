package no.cantara.cs.dto;

import no.cantara.cs.dto.event.ExtractedEventsStore;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 2015-09-04.
 */
public class CheckForUpdateRequest {
    public String configLastChanged;
    public Map<String, String> envInfo;
    public String tags;
    public String clientName;
    public ExtractedEventsStore eventsStore;

    //jackson
    private CheckForUpdateRequest() {
    }

    public CheckForUpdateRequest(String configLastChanged) {
        this.configLastChanged = configLastChanged;
        this.envInfo = new HashMap<>();
    }

    public CheckForUpdateRequest(String configLastChanged, Map<String, String> envInfo) {
        this.configLastChanged = configLastChanged;
        this.envInfo = envInfo;
    }

    public CheckForUpdateRequest(String configLastChanged, Map<String, String> envInfo, String clientName) {
        this(configLastChanged, envInfo);
        this.clientName = clientName;
    }

    public CheckForUpdateRequest(String configLastChanged, Map<String, String> envInfo, String clientName,
                                 ExtractedEventsStore eventsStore) {
        this(configLastChanged, envInfo);
        this.clientName = clientName;
        this.eventsStore= eventsStore;
    }

    public CheckForUpdateRequest(String configLastChanged, Map<String, String> envInfo, String tags,
                                 String clientName) {
        this(configLastChanged, envInfo, clientName);
        this.tags = tags;
    }
}
