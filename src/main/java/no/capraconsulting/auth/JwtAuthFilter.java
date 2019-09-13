package no.capraconsulting.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import no.capraconsulting.config.PropertiesHelper;
import no.capraconsulting.auth.JwtFilter;
import java.util.List;
import java.util.Properties;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;

@Provider
@JwtFilter
public class JwtAuthFilter implements ContainerRequestFilter {

    private static final String AUTH_HEADER_KEY = "Authorization";
    private static final String AUTH_HEADER_TOKEN = "Bearer "; // With trailing space to separate token

    private static Logger LOG = LoggerFactory.getLogger(JwtAuthFilter.class);
    
    private static Properties properties = PropertiesHelper.getProperties();
 
    @Override
    public void filter(ContainerRequestContext requestContext) {
		if (requestContext.getMethod().equals("OPTIONS")) return; // No auth for OPTIONS method because of CORS

        final MultivaluedMap<String, String> headers = requestContext.getHeaders();

        // Fetch authorization header
        final List<String> authorization = headers.get(AUTH_HEADER_KEY);

		if(authorization == null) {
            LOG.debug("Header key not found");
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
			return;
		}

        // Get the authentication header. Tokens are supposed to be passed in the authentication header
		String header = authorization.get(0);
		
		// Validate the header and check the prefix
		if(header == null || !header.startsWith(AUTH_HEADER_TOKEN)) {
            LOG.debug("Token not found");
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
			return;
		}

        // Get and validate the token
		String token = header.replace(AUTH_HEADER_TOKEN, "");

 		String keyUrl = PropertiesHelper.getRequiredStringProperty(properties, "azure.auth.keys");
		HttpsJwks httpsJkws = new HttpsJwks(keyUrl);
    	HttpsJwksVerificationKeyResolver httpsJwksKeyResolver = new HttpsJwksVerificationKeyResolver(httpsJkws);

 		String issuer = PropertiesHelper.getRequiredStringProperty(properties, "azure.auth.issuer");
 		String audience = PropertiesHelper.getRequiredStringProperty(properties, "azure.auth.audience");
		JwtConsumer jwtConsumer = new JwtConsumerBuilder()
			.setRequireExpirationTime()
			.setAllowedClockSkewInSeconds(600) // Allow some leeway in validating time based claims to account for clock skew
			.setRequireSubject() 
			.setExpectedIssuer(issuer)
			.setExpectedAudience(audience)
			.setVerificationKeyResolver(httpsJwksKeyResolver)
			.build();

		try {
			JwtClaims claims = jwtConsumer.processToClaims(token);
            requestContext.setProperty("claims", claims);
		} catch (InvalidJwtException e) {
			LOG.debug("JWT validation failed");
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
		}
    }
}
