package cucumber.examples.java.websockets;

import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.handler.EmbeddedResourceHandler;

import java.io.IOException;

public class TemperatureServer {
    private final WebServer webServer;

    public TemperatureServer(int port) {
        webServer = WebServers.createWebServer(port);
        webServer.add(new EmbeddedResourceHandler("web"));
        webServer.add("/temperature", new BaseWebSocketHandler() {
            @Override
            public void onMessage(WebSocketConnection connection, String msg) throws Throwable {
                String[] parts = msg.split(":");
                double t = Double.parseDouble(parts[1]);
                if (parts[0].equals("celcius")) {
                    double f = (9.0 / 5.0) * t + 32;
                    connection.send("fahrenheit:" + f);
                }
            }
        });
    }

    public void start() throws IOException {
        webServer.start();
    }

    public void stop() throws IOException {
        webServer.stop();
    }

    public static void main(String[] args) throws IOException {
        new TemperatureServer(9988).start();
    }
}
