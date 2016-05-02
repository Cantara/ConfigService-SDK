package no.cantara.cs.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3EncryptionClient;
import com.amazonaws.services.s3.model.CryptoConfiguration;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.KMSEncryptionMaterialsProvider;

import no.cantara.cs.dto.DownloadItem;

/**
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 2015-07-13.
 */
public class DownloadUtil {
    private static final Logger log = LoggerFactory.getLogger(DownloadUtil.class);

/*   Example go handling different proxies
    public void setProxy() {
        if (isUseHTTPProxy()) {
            // HTTP/HTTPS Proxy
            System.setProperty("http.proxyHost", getHTTPHost());
            System.setProperty("http.proxyPort", getHTTPPort());
            System.setProperty("https.proxyHost", getHTTPHost());
            System.setProperty("https.proxyPort", getHTTPPort());
            if (isUseHTTPAuth()) {
                String encoded = new String(Base64.encodeBase64((getHTTPUsername() + ":" + getHTTPPassword()).getBytes()));
                con.setRequestProperty("Proxy-Authorization", "Basic " + encoded);
                Authenticator.setDefault(new ProxyAuth(getHTTPUsername(), getHTTPPassword()));
            }
        }
        if (isUseSOCKSProxy()) {
            // SOCKS Proxy
            System.setProperty("socksProxyHost", getSOCKSHost());
            System.setProperty("socksProxyPort", getSOCKSPort());
            if (isUseSOCKSAuth()) {
                System.setProperty("java.net.socks.username", getSOCKSUsername());
                System.setProperty("java.net.socks.password", getSOCKSPassword());
                Authenticator.setDefault(new ProxyAuth(getSOCKSUsername(), getSOCKSPassword()));
            }
        }
    }
     */

    public static List<Path> downloadAllFiles(List<DownloadItem> downloadItems, String targetDirectory) {
        Path path;
        List<Path> paths = new ArrayList<>(downloadItems.size());
        for (DownloadItem downloadItem : downloadItems) {
            log.debug("Downloading {}", downloadItem);
            path = downloadFile(downloadItem, targetDirectory);
            paths.add(path);
        }
        return paths;
    }

    public static Path downloadFile(DownloadItem downloadItem, String targetDirectory) {
        return downloadFile(downloadItem.url, downloadItem.filename(), downloadItem.username, downloadItem.password, targetDirectory);
    }

    /**
     * http://www.codejava.net/java-se/networking/use-httpurlconnection-to-download-file-from-an-http-url
     * Downloads a file from a URL
     *
     * @param sourceUrl        HTTP URL of the file to be downloaded
     * @param filenameOverride filename to store the downloaded file as
     * @param username         username  to authenticate against the server
     * @param password         password  to authenticate against the server
     * @param targetDirectory  path of the directory to save the file
     * @return Path to the downloaded file
     */
    public static Path downloadFile(String sourceUrl, String filenameOverride, String username, String password, String targetDirectory) {
        URI uri;
        try {
            uri = new URI(sourceUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        String scheme = uri.getScheme();
        if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
            return downloadFileWithHttp(uri, filenameOverride, username, password, targetDirectory);
        }
        if ("s3".equalsIgnoreCase(scheme)) {
            return downloadFileWithS3(uri, filenameOverride, targetDirectory);
        }
        throw new RuntimeException("Unsupported scheme: " + scheme);
    }

    private static Path downloadFileWithHttp(URI uri, String filenameOverride, String username, String password, String targetDirectory) {
        HttpURLConnection httpConn;
        try {
            httpConn = (HttpURLConnection) uri.toURL().openConnection();
            if (username != null && password != null) {
                String authorizationValue = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
                httpConn.setRequestProperty("Authorization", authorizationValue);
            }
            int responseCode = httpConn.getResponseCode();

            // always check HTTP response code first
            if (responseCode != HttpURLConnection.HTTP_OK) {
                log.info("Could not download. Server replied with {} {}", responseCode, httpConn.getResponseMessage());
                httpConn.disconnect();
                return null;
            }

            String fileName = filenameOverride;
            if (fileName == null || fileName.isEmpty()) {
                fileName = "";
                String disposition = httpConn.getHeaderField("Content-Disposition");
                log.debug("Content-Disposition = " + disposition);
                if (disposition != null) {
                    // extracts file name from header field
                    int index = disposition.indexOf("filename=");
                    if (index > 0) {
                        fileName = disposition.substring(index + 10, disposition.length() - 1);
                    }
                } else {
                    // extracts file name from URL
                    String path = uri.getPath();
                    fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
                }
            }

            log.debug("Content-Type = " + httpConn.getContentType());
            log.debug("Content-Length = " + httpConn.getContentLength());
            log.debug("fileName = " + fileName);

            // opens input stream from the HTTP connection
            File targetDirectoryAsFile = new File(targetDirectory);
            if (!targetDirectoryAsFile.exists()) {
                targetDirectoryAsFile.mkdirs();
            }

            // opens an output stream to save into file
            String targetPath = targetDirectory + File.separator + fileName;

            try (FileOutputStream outputStream = new FileOutputStream(targetPath);
                 InputStream inputStream = httpConn.getInputStream()) {
                final int BUFFER_SIZE = 4096;
                int bytesRead;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            log.info("File downloaded to {}", targetPath);
            httpConn.disconnect();
            return new File(targetPath).toPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path downloadFileWithS3(URI uri, String filenameOverride, String targetDirectory) {
        S3Spec spec = S3Spec.parse(uri);

        String fileName = filenameOverride;
        if (fileName == null || fileName.isEmpty()) {
            String path = uri.getPath();
            fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
        }

        File targetDirectoryAsFile = new File(targetDirectory);
        if (!targetDirectoryAsFile.exists()) {
            targetDirectoryAsFile.mkdirs();
        }
        File targetFile = new File(targetDirectoryAsFile, fileName);

        AmazonS3Client client = createS3Client(spec);
        if (spec.region != null) {
            client.setRegion(RegionUtils.getRegion(spec.region));
        }
        client.getObject(new GetObjectRequest(spec.s3Bucket, spec.s3Object), targetFile);

        log.info("File downloaded to {}", targetFile);

        return targetFile.toPath();
   }

    private static AmazonS3Client createS3Client(S3Spec spec) {

        // Enable client-side encryption if CMK ID (Customer Master Key) is specified.
        if (spec.cmkId != null) {
            Region region = RegionUtils.getRegion(spec.region);
            CryptoConfiguration cryptoConfig = new CryptoConfiguration().withAwsKmsRegion(region);
            return new AmazonS3EncryptionClient(new DefaultAWSCredentialsProviderChain(),
                                                new KMSEncryptionMaterialsProvider(spec.cmkId),
                                                cryptoConfig).withRegion(region);
        }

        return new AmazonS3Client();
    }

    static class S3Spec {
        String s3Bucket;
        String s3Object;
        String region;
        String cmkId;

        private S3Spec(String s3Bucket, String s3Object, String region, String cmkId) {
            this.s3Bucket = s3Bucket;
            this.s3Object = s3Object;
            this.region = region;
            this.cmkId = cmkId;
        }

        /**
         * URI is of the following form: s3://[s3Bucket]/[s3Object]?[query]
         * <p>
         * Supported query parameters are "region" (AWS region) and "cmkid" (ID or alias of Customer Master Key in KMS).
         * <p>
         * For instance: s3://mybucket/foo/bar/file.txt?region=eu-west-1&cmkid=123abc
         */
        static S3Spec parse(URI uri) {
            String s3Bucket = uri.getAuthority();
            String s3Object = uri.getPath().substring(1);
            Map<String, String> params = parseQuery(uri.getQuery());

            return new S3Spec(s3Bucket, s3Object, params.get("region"), params.get("cmkid"));
        }

        private static Map<String, String> parseQuery(String query) {
            Map<String, String> result = new LinkedHashMap<>();
            if (query == null) {
                return result;
            }
            for (String part : query.split("&")) {
                int i = part.indexOf("=");
                if (i != -1) {
                    result.put(part.substring(0, i), part.substring(i + 1));
                }
            }
            return result;
        }
    }
}
