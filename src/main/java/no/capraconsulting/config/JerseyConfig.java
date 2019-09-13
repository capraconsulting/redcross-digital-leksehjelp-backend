package no.capraconsulting.config;

import org.glassfish.jersey.server.ResourceConfig;
import java.util.Properties;

public class JerseyConfig extends ResourceConfig {

    public JerseyConfig(Properties properties) {
        
        if (PropertiesHelper.getBooleanProperty(properties, "cors.filter.enabled", false)) {
            this.register(createCORSFilter(properties));
        }

        this.packages("no.capraconsulting");
    }

    private static CORSFilter createCORSFilter(Properties properties) {
        String allowOrigin = PropertiesHelper.getRequiredStringProperty(properties, "cors.allow.origin");
        String allowHeaders = PropertiesHelper.getRequiredStringProperty(properties, "cors.allow.headers");
        String allowMethods = PropertiesHelper.getRequiredStringProperty(properties, "cors.allow.methods");
        String allowCredentials = PropertiesHelper.getRequiredStringProperty(properties, "cors.allow.credentials");
        String maxAge = PropertiesHelper.getRequiredStringProperty(properties, "cors.max.age");
        return new CORSFilter(allowOrigin, allowHeaders, allowMethods, allowCredentials, maxAge);
    }
}
