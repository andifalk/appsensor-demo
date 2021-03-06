package info.novatec.appsensor.server.websocket;

import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A simple dashboard for the websocket implementation.
 *
 * @author John Melton (jtmelton@gmail.com) http://www.jtmelton.com/
 */
@Component
@ServerEndpoint(value = "/dashboard")
public class WebSocketDashboard {

    private static Queue<Session> queue = new ConcurrentLinkedQueue<>();

    @OnOpen
    public void onOpen(final Session session) {
        queue.add(session);

        removeClosedSessions();

        System.err.println("Opened connection with client: " + session.getId());
    }

    @OnMessage
    public synchronized void onMessage(String message, Session session) {
        System.err.println("New message from Client " + session.getId() + ": " + message);

        //should echo back whatever is heard from any client to all clients
        for (Session localSession : queue) {
            if (localSession.isOpen()) {
                try {
                    localSession.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @OnClose
    public void onClose(Session session) {
        queue.remove(session);

        removeClosedSessions();

        System.err.println("Closed connection with client: " + session.getId());
    }

    private void removeClosedSessions() {
        Collection<Session> closedSessions = new ArrayList<>();

        for (Session session : queue) {
            if (! session.isOpen()) {
                closedSessions.add(session);
            }
        }

        queue.removeAll(closedSessions);
    }

    @OnError
    public void onError(Throwable exception, Session session) {
        System.err.println("Error for client: " + session.getId());
        exception.printStackTrace();
    }
}
