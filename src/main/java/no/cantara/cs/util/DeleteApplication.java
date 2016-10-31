package no.cantara.cs.util;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.cantara.cs.client.ConfigServiceAdminClient;
import no.cantara.cs.dto.Application;

/**
 * Deletes an application.
 *
 * @author Sindre Mehus
 */
public class DeleteApplication {

    private static final Logger log = LoggerFactory.getLogger(DeleteApplication.class);

    private final ConfigServiceAdminClient adminClient;

    public DeleteApplication(ConfigServiceAdminClient adminClient) {
        this.adminClient = adminClient;
    }

    public DeleteApplication(Environment environment) {
        this(new ConfigServiceAdminClient(environment));
    }

    public void deleteApplication(Path applicationFile) throws Exception {
        Application application = JsonUtil.readApplicationFromFile(applicationFile);

        adminClient.deleteApplication(application.id);
        log.info("Deleted application {}", application.toString());
    }
}
