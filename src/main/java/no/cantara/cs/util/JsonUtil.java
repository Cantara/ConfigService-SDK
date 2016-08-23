package no.cantara.cs.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.cantara.cs.dto.Application;
import no.cantara.cs.dto.ApplicationConfig;

/**
 * @author <a href="mailto:asbjornwillersrud@gmail.com">Asbjørn Willersrud</a> 01/04/2016.
 */
public class JsonUtil {

    public static ApplicationConfig readConfigFromFile(Path path) throws IOException {
        return new ObjectMapper().readValue(Files.readAllBytes(path), ApplicationConfig.class);
    }

    public static Application readApplicationFromFile(Path path) throws IOException {
        return new ObjectMapper().readValue(Files.readAllBytes(path), Application.class);
    }

    public static String toJson(Object object) {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
