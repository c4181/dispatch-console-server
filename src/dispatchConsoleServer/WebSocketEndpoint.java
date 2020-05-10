package dispatchConsoleServer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;


@ServerEndpoint("/socket")
public class WebSocketEndpoint {

  private Session session;
  private static Set<WebSocketEndpoint> chatEndpoints = new CopyOnWriteArraySet<>();
  private static HashMap<String, String> users = new HashMap<>();

  @OnOpen
  public void onOpen(Session session, @PathParam("username") String username) throws IOException {
    this.session = session;
    chatEndpoints.add(this);
    users.put(session.getId(), username);
    this.session.getBasicRemote()
        .sendText("Hello, this is server. You are client " + this.session.getId());
  }

  @OnClose
  public void onClose(Session session) {
    chatEndpoints.remove(this);
  }

  @OnMessage
  public void onMessage(String message, Session session) {
    try {
      broadcast(message, this);
    } catch (IOException | EncodeException e) {
      e.printStackTrace();
    }
  }

  private static void broadcast(String message, WebSocketEndpoint sender) throws IOException, EncodeException {
    chatEndpoints.forEach(endpoint -> {
      synchronized (endpoint) {
        try {
          if (endpoint != sender)
          {
            endpoint.session.getBasicRemote().sendText(message);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }
}
