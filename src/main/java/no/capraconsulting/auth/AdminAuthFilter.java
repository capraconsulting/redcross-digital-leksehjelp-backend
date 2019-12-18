package no.capraconsulting.auth;


import no.capraconsulting.repository.AdminRepository;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.sql.SQLException;

@Provider
@AdminFilter
@Priority(Priorities.AUTHORIZATION)
public class AdminAuthFilter implements ContainerRequestFilter {

    private static Logger LOG = LoggerFactory.getLogger(JwtAuthFilter.class);
    @Override
    public void filter(ContainerRequestContext requestContext) {

        JwtClaims claims = (JwtClaims) requestContext.getProperty("claims");
        String oid = null;
        try {
            oid = claims.getClaimValue("oid", String.class);
        } catch (MalformedClaimException e) {
            requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST).build());
        }

        if(oid == null){
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        } else {
            try {
                JSONArray test = AdminRepository.getUserRole(oid);
                LOG.debug(test.toString());
            } catch (SQLException e) {
                LOG.error(e.getMessage());
                requestContext.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
            }
        }
    }
}
