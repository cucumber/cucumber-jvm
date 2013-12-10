package cucumber.examples.java.websockets;

import cucumber.api.java.After;
import cucumber.api.java.Before;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class ServerHooks {
    public static final int PORT = 8887;

    private TemperatureServer temperatureServer;

    @Before
    public void startServer() throws IOException, ExecutionException, InterruptedException {
        temperatureServer = new TemperatureServer(PORT);
        temperatureServer.start();
    }

    @After
    public void stopServer() throws IOException, ExecutionException, InterruptedException {
        temperatureServer.stop();
    }
}
