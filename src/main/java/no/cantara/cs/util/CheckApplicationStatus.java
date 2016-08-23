package no.cantara.cs.util;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.cantara.cs.client.ConfigServiceAdminClient;
import no.cantara.cs.dto.ApplicationStatus;

/**
 * @author Asbjørn Willersrud
 */
public class CheckApplicationStatus {

    private final ConfigServiceAdminClient adminClient;

    public CheckApplicationStatus(ConfigServiceAdminClient adminClient) {
        this.adminClient = adminClient;
    }

    public CheckApplicationStatus(Environment environment) {
        this(new ConfigServiceAdminClient(environment));
    }

    public void checkApplicationStatus(String artifactId) throws IOException {
        ApplicationStatus applicationStatus = adminClient.getApplicationStatus(artifactId);
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(applicationStatus));
    }
}
