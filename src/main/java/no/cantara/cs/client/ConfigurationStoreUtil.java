package no.cantara.cs.client;

import no.cantara.cs.dto.NamedPropertiesStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 2015-08-07.
 */
public class ConfigurationStoreUtil {
    private static final Logger log = LoggerFactory.getLogger(DownloadUtil.class);

    public static List<Path> toFiles(List<NamedPropertiesStore> configurationStores, String targetDirectory) {
        try {
            Path path;
            List<Path> paths = new ArrayList<>(configurationStores.size());
            for (NamedPropertiesStore store : configurationStores) {
                path = toFile(store, targetDirectory);
                paths.add(path);
            }
            return paths;
        } catch (IOException e) {
            String msg = "toFiles failed. targetDirectory=" + targetDirectory;
            log.error(msg, e);
            throw new RuntimeException(msg + ". " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    public static Path toFile(NamedPropertiesStore store, String targetDirectoryPath) throws IOException {
        Properties props = new Properties();
        props.putAll(store.properties);

        File targetDirectory = new File(targetDirectoryPath);
        targetDirectory.mkdirs();
        File targetFile = new File(targetDirectory, store.fileName);

        log.debug("Writing NamedPropertiesStore to {}", targetFile);
        OutputStream out = new FileOutputStream(targetFile);
        props.store(out, null);
        return targetFile.toPath();
    }
}
