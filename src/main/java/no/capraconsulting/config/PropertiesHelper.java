package no.capraconsulting.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class PropertiesHelper {

    private static final Logger log = LoggerFactory.getLogger(PropertiesHelper.class);

    public static final String DB_CONTAINER_NAME = "db.container.name";
    public static final String DB_NAME = "db.name";
    public static final String DB_PORT = "db.port";
    public static final String DB_USER = "db.user";
    public static final String DB_PASSWORD = "db.password";
    public static final String AZURE_AUTH_KEYS = "azure.auth.keys";
    public static final String AZURE_AUTH_ISSUER = "azure.auth.issuer";
    public static final String AZURE_AUTH_AUDIENCE = "azure.auth.audience";
    public static final String MIXPANEL_PROJECT_TOKEN = "mixpanel.project.token";
    public static final String MAIL_USERNAME = "mail.username";
    public static final String MAIL_PASSWORD = "mail.password";
    public static final String MAIL_APIKEY = "mail.apikey";
    public static final String ENVIRONMENT = "environment";
    public static final String NICKNAME_COUNTER_MAX = "nickname.counter.max";
    private static String CLIENT_ID = "msgraph.auth.client.id";
    private static String SCOPE = "msgraph.auth.scope";
    private static String CLIENT_SECRET = "msgraph.auth.client.secret";
    private static String TENANT_GUID = "msgraph.auth.tenant.guid";
    private static String AUTHORITY = "msgraph.auth.authority.url";

    public static Properties getProperties() {
        Properties properties = new Properties();
        properties.putAll(getPropertiesFromClasspathFile("application.properties"));
        properties.putAll(getPropertiesFromOptionalClasspathFile("application-test.properties"));
        properties.putAll(getPropertiesFromFile(new File("application-override.properties")));
        return properties;
    }

    static Properties getPropertiesFromClasspathFile(String filename) {
        Properties properties = new Properties();
        try {
            properties.load(new InputStreamReader(PropertiesHelper.class.getClassLoader().getResourceAsStream(filename), StandardCharsets.UTF_8));
        } catch (NullPointerException e) {
            throw new RuntimeException(filename + " not found on classpath.", e);
        } catch (IOException e) {
            throw new RuntimeException("IOException while reading " + filename + " from classpath.", e);
        }
        return properties;
    }

    private static Properties getPropertiesFromOptionalClasspathFile(String filename) {
        Properties properties = new Properties();
        try {
            properties.load(new InputStreamReader(PropertiesHelper.class.getClassLoader().getResourceAsStream(filename), StandardCharsets.UTF_8));
        } catch (NullPointerException e) {
            return properties;
        } catch (IOException e) {
            throw new RuntimeException("IOException while reading " + filename + " from classpath.", e);
        }
        return properties;
    }

    private static Properties getPropertiesFromFile(File file) {
        Properties properties = new Properties();
        if (file.exists()) {
            try (InputStreamReader input = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                properties.load(input);
            } catch (Exception e) {
                log.warn("Failed to load properties from {}", file, e);
            }
        }
        return properties;
    }

    public static String getRequiredStringProperty(final Properties properties, String propertyKey) throws IllegalStateException {
        String result = getStringProperty(properties, propertyKey, null);
        return checkRequiredProperty(propertyKey, result);
    }

    public static Integer getRequiredIntProperty(final Properties properties, String propertyKey) throws IllegalStateException {
        Integer result = getIntProperty(properties, propertyKey, null);
        return checkRequiredProperty(propertyKey, result);
    }

    public static Long getRequiredLongProperty(final Properties properties, String propertyKey) throws IllegalStateException {
        Long result = getLongProperty(properties, propertyKey, null);
        return checkRequiredProperty(propertyKey, result);
    }

    public static Boolean getRequiredBooleanProperty(final Properties properties, String propertyKey) throws IllegalStateException {
        Boolean result = getBooleanProperty(properties, propertyKey, null);
        return checkRequiredProperty(propertyKey, result);
    }

    private static <T> T checkRequiredProperty(String propertyKey, T result) {
        if (result == null) {
            throw new IllegalStateException(String.format("Property '%s' not found.", propertyKey));
        }
        return result;
    }

    public static String getStringProperty(final Properties properties, String propertyKey, String defaultValue) {
        String property = System.getProperty(propertyKey);
        if (property == null) {
            property = properties.getProperty(propertyKey, defaultValue);
        }
        return property;
    }

    public static Integer getIntProperty(final Properties properties, String propertyKey, Integer defaultValue) {
        String property = getStringProperty(properties, propertyKey, null);
        if (property == null) {
            return defaultValue;
        }
        return Integer.valueOf(property);
    }

    public static Long getLongProperty(final Properties properties, String propertyKey, Long defaultValue) {
        String property = getStringProperty(properties, propertyKey, null);
        if (property == null) {
            return defaultValue;
        }
        return Long.valueOf(property);
    }

    public static Boolean getBooleanProperty(final Properties properties, String propertyKey, Boolean defaultValue) {
        String property = getStringProperty(properties, propertyKey, null);
        if (property == null) {
            return defaultValue;
        }
        return Boolean.valueOf(property);
    }
}
