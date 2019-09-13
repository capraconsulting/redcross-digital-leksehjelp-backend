package no.capraconsulting.chat;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import javax.websocket.server.ServerContainer;

public class ChatServer extends Thread {

    @Override
    public void run()
    {
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(3002);
        server.addConnector(connector);

        // Setup the basic application "context" for this application at "/"
        // This is also known as the handler tree (in jetty speak)
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        try
        {
            // Initialize javax.websocket layer
            ServerContainer wscontainer = WebSocketServerContainerInitializer.configureContext(context);
            ServletHolder holderEvents = new ServletHolder("ws-events", ChatServlet.class);
            // Add WebSocket endpoint to javax.websocket layer
            //wscontainer.addEndpoint(ChatServlet.class);

            context.addServlet(holderEvents, "/events/*");
            server.start();
            server.dump(System.err);
            server.join();
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);
        }
    }
}
