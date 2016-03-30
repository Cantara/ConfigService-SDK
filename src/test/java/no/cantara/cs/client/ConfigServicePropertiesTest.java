package no.cantara.cs.client;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 2016-03-30.
 */
public class ConfigServicePropertiesTest {
    @BeforeMethod
    public void cleanEnv() {
        setEnv(Collections.emptyMap());
    }

    @Test
    public void testOnlySystemPropertiesOK() {
        ConfigServiceProperties csProperties = new ConfigServiceProperties(null);
        assertNull(csProperties.getServiceConfigUrl());
        assertNull(csProperties.getUsername());
        assertNull(csProperties.getPassword());

        Map<String, String> newenv = new HashMap<>(3);
        newenv.put(ConfigServiceProperties.CONFIG_SERVICE_URL_KEY, "http://someUrl.net");
        newenv.put(ConfigServiceProperties.CONFIG_SERVICE_USERNAME_KEY, "username1");
        newenv.put(ConfigServiceProperties.CONFIG_SERVICE_PASSWORD_KEY, "pass1");
        setEnv(newenv);
        assertEquals(csProperties.getServiceConfigUrl(), newenv.get(ConfigServiceProperties.CONFIG_SERVICE_URL_KEY));
        assertEquals(csProperties.getUsername(), newenv.get(ConfigServiceProperties.CONFIG_SERVICE_USERNAME_KEY));
        assertEquals(csProperties.getPassword(), newenv.get(ConfigServiceProperties.CONFIG_SERVICE_PASSWORD_KEY));
    }
    @Test
    public void testOnlyPropertiesFileOK() {
        ConfigServiceProperties csProperties = new ConfigServiceProperties("ConfigServicePropertiesTest.properties");
        assertEquals(csProperties.getServiceConfigUrl(), "https://urlFromFile.org");
        assertEquals(csProperties.getUsername(), "usernameFromFile");
        assertEquals(csProperties.getPassword(), "passwordFromFile");
    }
    @Test
    public void testEnvTakesPrecedence() {
        ConfigServiceProperties csProperties = new ConfigServiceProperties("ConfigServicePropertiesTest.properties");
        Map<String, String> newenv = new HashMap<>(3);
        newenv.put(ConfigServiceProperties.CONFIG_SERVICE_URL_KEY, "http://someUrl.net");
        newenv.put(ConfigServiceProperties.CONFIG_SERVICE_USERNAME_KEY, "username1");
        newenv.put(ConfigServiceProperties.CONFIG_SERVICE_PASSWORD_KEY, "pass1");
        setEnv(newenv);
        assertEquals(csProperties.getServiceConfigUrl(), newenv.get(ConfigServiceProperties.CONFIG_SERVICE_URL_KEY));
        assertEquals(csProperties.getUsername(), newenv.get(ConfigServiceProperties.CONFIG_SERVICE_USERNAME_KEY));
        assertEquals(csProperties.getPassword(), newenv.get(ConfigServiceProperties.CONFIG_SERVICE_PASSWORD_KEY));
        assertEquals(csProperties.getArtifactId(), "artifactIdFromFile");
        assertEquals(csProperties.getClientId(), "clientIdFromFile");
    }


    /**
     * Dirty hack to change in-mem copy of env variables for testing only. Will not change the env variables on the system.
     * http://stackoverflow.com/questions/318239/how-do-i-set-environment-variables-from-java
     */
    protected static void setEnv(Map<String, String> newenv) {
        try  {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>)     theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        }
        catch (NoSuchFieldException e) {
            try {
                Class[] classes = Collections.class.getDeclaredClasses();
                Map<String, String> env = System.getenv();
                for(Class cl : classes) {
                    if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                        Field field = cl.getDeclaredField("m");
                        field.setAccessible(true);
                        Object obj = field.get(env);
                        Map<String, String> map = (Map<String, String>) obj;
                        map.clear();
                        map.putAll(newenv);
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
