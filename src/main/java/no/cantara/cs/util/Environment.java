package no.cantara.cs.util;

/**
 * A ConfigService environment.
 *
 * @author Sindre Mehus
 */
public interface Environment {

    /**
     * @return Name of the environment, e.g., "PROD".
     */
    String getName();

    /**
     * @return Config Service username.
     */
    String getUsername();

    /**
     * @return Config Service password.
     */
    String getPassword();

    /**
     * @return Config Service base URL.
     */
    String getUrl();
}
