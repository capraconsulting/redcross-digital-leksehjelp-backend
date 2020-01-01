package no.capraconsulting.client;

import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.google.gson.JsonObject;
import no.capraconsulting.config.PropertiesHelper;

import java.util.Properties;


public class MSGraphClient {
    private MSGraphAuth authProvider;
    private IGraphServiceClient client;
    private static Properties properties = PropertiesHelper.getProperties();
    private static String CLIENT_ID;
    private static String SCOPE;
    private static String CLIENT_SECRET;
    private static String TENANT_GUID;
    private static String AUTHORITY;


    public MSGraphClient() {
        setProperties();
        this.authProvider = new MSGraphAuth(TENANT_GUID, AUTHORITY, CLIENT_ID, SCOPE, CLIENT_SECRET);
        this.client = GraphServiceClient.builder().authenticationProvider(authProvider).buildClient();
    }

    private void setProperties(){
        CLIENT_ID = PropertiesHelper.getRequiredStringProperty(properties, "msgraph.auth.client.id");
        SCOPE = PropertiesHelper.getRequiredStringProperty(properties, "msgraph.auth.scope");
        CLIENT_SECRET = PropertiesHelper.getRequiredStringProperty(properties, "msgraph.auth.client.secret");
        TENANT_GUID = PropertiesHelper.getRequiredStringProperty(properties, "msgraph.auth.tenant.guid");
        AUTHORITY = PropertiesHelper.getRequiredStringProperty(properties, "msgraph.auth.authority.url");
    }

    public JsonObject getUserIdByEmail(String email) {
        String searchUrl = String.format("/users/?$filter=mail%%20eq%%20'%s'", email);
        return client.customRequest(searchUrl).buildRequest().get();
    }


}
