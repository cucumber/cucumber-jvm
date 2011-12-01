package cucumber.examples.java.websockets;

import cucumber.annotation.After;
import cucumber.annotation.Before;

import java.io.IOException;

public class ServerHooks {
    public static final int PORT = 8887;

    private TemperatureServer temperatureServer;

    @Before
    public void startServer() throws IOException {
        temperatureServer = new TemperatureServer(PORT);
        temperatureServer.start();
    }

    @After
    public void stopServer() throws IOException {
        temperatureServer.stop();
    }
}
