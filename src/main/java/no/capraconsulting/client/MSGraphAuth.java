package no.capraconsulting.client;

import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.http.IHttpRequest;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;

public class MSGraphAuth implements IAuthenticationProvider {
    private String tenant;
    private String authority;
    private String clientId;
    private String scope;
    private String clientSecret;
    private static String TOKEN_ENDPOINT = "/oauth2/v2.0/token";


    public MSGraphAuth(String tenant, String authority, String clientId, String scope, String clientSecret) {
        this.tenant = tenant;
        this.authority = authority;
        this.clientId = clientId;
        this.scope = scope;
        this.clientSecret = clientSecret;
    }


    @Override
    public void authenticateRequest(IHttpRequest request) {
        String accessToken = getAccessToken();
        request.addHeader("Authorization", "Bearer " + accessToken);

    }

    private String getAccessToken() {
       String accessToken = "";
       try {
           OAuthClientRequest authRequest = getTokenRequestMessage();
           accessToken = getAccessTokenNewRequest(authRequest);
       } catch (OAuthSystemException | OAuthProblemException e ) {
          e.printStackTrace();
       }

       return accessToken;
    }

    private OAuthClientRequest getTokenRequestMessage() throws OAuthSystemException {
        String tokenUrl = getAuthorityUrl() + TOKEN_ENDPOINT;
        OAuthClientRequest.TokenRequestBuilder token = OAuthClientRequest
            .tokenLocation(tokenUrl)
            .setClientId(this.clientId)
            .setGrantType(GrantType.CLIENT_CREDENTIALS)
            .setScope(this.scope)
            .setClientSecret(this.clientSecret);
        return token.buildBodyMessage();
    }

    private String getAuthorityUrl() {
        return (this.authority + this.tenant);
    }

    private String getAccessTokenNewRequest(OAuthClientRequest request) throws OAuthSystemException, OAuthProblemException {
        OAuthClient client = new OAuthClient(new URLConnectionClient());
        return client.accessToken(request).getAccessToken();
    }
}
