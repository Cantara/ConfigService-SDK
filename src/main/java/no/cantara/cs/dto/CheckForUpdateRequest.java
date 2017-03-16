package no.cantara.cs.dto;

import no.cantara.cs.dto.event.ExtractedEventsStore;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 2015-09-04.
 */
public class CheckForUpdateRequest {
    public String clientId;
    public String clientSecret;
    public String configLastChanged;
    public Map<String, String> envInfo;
    public String tags;
    public String clientName;
    public ExtractedEventsStore eventsStore;

    //jackson
    private CheckForUpdateRequest() {}

    public CheckForUpdateRequest(String clientId, String configLastChanged, String clientSecret) {
        this.clientId = clientId;
        this.configLastChanged = configLastChanged;
        this.clientSecret = clientSecret;
    }

    public CheckForUpdateRequest withClientName(String clientName) {
        this.clientName = clientName;
        return this;
    }
    public CheckForUpdateRequest withTags(String tags) {
        this.tags = tags;
        return this;
    }
    public CheckForUpdateRequest withEnvInfo(Map<String, String> envInfo) {
        this.envInfo = envInfo;
        return this;
    }
    public CheckForUpdateRequest withEventsStore(ExtractedEventsStore eventsStore) {
        this.eventsStore = eventsStore;
        return this;
    }


    @Deprecated
    public CheckForUpdateRequest(String configLastChanged) {
        this.configLastChanged = configLastChanged;
        this.envInfo = new HashMap<>();
    }
    @Deprecated
    public CheckForUpdateRequest(String configLastChanged, Map<String, String> envInfo) {
        this.configLastChanged = configLastChanged;
        this.envInfo = envInfo;
    }
    @Deprecated
    public CheckForUpdateRequest(String configLastChanged, Map<String, String> envInfo, String clientName) {
        this(configLastChanged, envInfo);
        this.clientName = clientName;
    }
    @Deprecated
    public CheckForUpdateRequest(String configLastChanged, Map<String, String> envInfo, String clientName,
                                 ExtractedEventsStore eventsStore) {
        this(configLastChanged, envInfo);
        this.clientName = clientName;
        this.eventsStore= eventsStore;
    }
    @Deprecated
    public CheckForUpdateRequest(String configLastChanged, Map<String, String> envInfo, String tags,
                                 String clientName) {
        this(configLastChanged, envInfo, clientName);
        this.tags = tags;
    }
}
