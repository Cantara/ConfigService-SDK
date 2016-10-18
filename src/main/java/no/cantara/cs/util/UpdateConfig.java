package no.cantara.cs.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import no.cantara.cs.client.ConfigServiceAdminClient;
import no.cantara.cs.dto.Application;
import no.cantara.cs.dto.ApplicationConfig;

/**
 * Updates an existing application config.
 * <p>
 * NOTE! Before running this utility, make sure to run {@link ExportConfigServerData} to get the latest config.
 *
 * @author <a href="mailto:asbjornwillersrud@gmail.com">Asbjørn Willersrud</a> 01/04/2016.
 */
public class UpdateConfig {

    private static final Logger log = LoggerFactory.getLogger(UpdateConfig.class);

    private final ConfigServiceAdminClient adminClient;

    public UpdateConfig(ConfigServiceAdminClient adminClient) {
        this.adminClient = adminClient;
    }

    public UpdateConfig(Environment environment) {
        this(new ConfigServiceAdminClient(environment));
    }

    public void updateConfig(Path configFile, Path applicationFile) throws Exception {
        updateConfig(configFile, applicationFile, Collections.emptyMap());
    }

    /**
     * @param rewrites A map of JSON rewrites.
     *                 Key: JSON path expression, e.g., "/downloadItems/0/url".
     *                 Value: The new value, e.g., "https://example.com/foo.jar".
     */
    public void updateConfig(Path configFile, Path applicationFile, Map<String, String> rewrites) throws Exception {
        Application application = JsonUtil.readApplicationFromFile(applicationFile);
        log.info("Updating config for application {}", application.toString());

        String configJson = rewriteJson(new String(Files.readAllBytes(configFile), StandardCharsets.UTF_8), rewrites);

        ApplicationConfig config = JsonUtil.readConfigFromString(configJson);
        config.setUpdated();
        log.info("New config {}", config.toString());

        adminClient.updateConfig(application.id, config);
    }

    private String rewriteJson(String json, Map<String, String> rewrites) throws IOException {
        if (rewrites.isEmpty()) {
            return json;
        }

        JsonNode root = new ObjectMapper().readTree(json);

        for (Map.Entry<String, String> entry : rewrites.entrySet()) {
            String path = entry.getKey();
            String rewriteTo = entry.getValue();

            int index = path.lastIndexOf("/");
            String parent = path.substring(0, index);
            String child = path.substring(index + 1);

            // Verify that path exists
            if (root.at(path).isMissingNode()) {
                log.error("JSON path {} not found. Ignoring it.", path);
                continue;
            }

            ObjectNode parentNode = (ObjectNode) root.at(parent);
            parentNode.put(child, rewriteTo);
            log.info("Rewrote {} to {} ", path, rewriteTo);
        }

        return root.toString();
    }
}
