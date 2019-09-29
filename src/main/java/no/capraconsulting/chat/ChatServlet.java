package no.capraconsulting.chat;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatServlet extends WebSocketServlet {
    final long TIMEOUT_LIMIT = 900000; //15 min
    static Logger LOG = LoggerFactory.getLogger(ChatServlet.class);
    @Override
    public void configure(WebSocketServletFactory factory)
    {
        factory.getPolicy().setIdleTimeout(TIMEOUT_LIMIT);
        factory.getExtensionFactory().unregister("permessage-deflate");
        factory.register(ChatEndpoint.class);
        RemoveEndedChats gc = new RemoveEndedChats();
        LOG.info("Started garbage collector");
        gc.run();
    }
}
