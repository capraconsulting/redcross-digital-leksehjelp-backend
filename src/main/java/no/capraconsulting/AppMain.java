package no.capraconsulting;

import no.capraconsulting.chat.ChatServer;
import no.capraconsulting.config.JerseyConfig;
import no.capraconsulting.config.JsonJettyErrorHandler;
import no.capraconsulting.config.PropertiesHelper;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Properties;

public class AppMain {
    private static final Logger log = LoggerFactory.getLogger(AppMain.class);

    static final String CONTEXT_PATH = System.getProperty("server.context.path", "/");
    private final Integer port;
    private final Properties properties;
    private Server server;

    AppMain(Integer port, Properties properties) {
        this.port = port;
        this.properties = properties;
    }

    public static void main(String[] args) throws Exception {
        Integer port = Integer.parseInt(System.getProperty("server.port", "8080"));
        Properties properties = PropertiesHelper.getProperties();
        ChatServer chatServer = new ChatServer();
        chatServer.start();
        new AppMain(port, properties).start();
        log.info("Server stopped");
    }

    void start() throws InterruptedException {
        log.debug("Starting server at port {}", port);
        server = new Server(port);
        server.setHandler(getServletContextHandler());
        server.setStopAtShutdown(true);

        try {
            server.start();
        } catch (Exception e) {
            log.error("Error during Jetty startup. Exiting", e);
            System.exit(1);
        }
        log.info("Server started at port {}.", port);
        server.join();
    }

    private ServletContextHandler getServletContextHandler() {
        ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.setContextPath(CONTEXT_PATH);

        // Add Jersey servlet to the Jetty context
        ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(new JerseyConfig(properties)));
        contextHandler.addServlet(jerseyServlet, "/*");
        //contextHandler.addServlet(ChatEndpoint.class.getName(), "/ws");

        // Error responses as application/json with proper charset
        contextHandler.setErrorHandler(new JsonJettyErrorHandler());

        return contextHandler;
    }

}
