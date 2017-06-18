package cucumber.examples.java.websockets;

import cucumber.api.java8.En;

public class ServerHooks implements En {
    static final int PORT = 8887;

    private TemperatureServer temperatureServer;

    public ServerHooks(){
        Before(() -> temperatureServer = new TemperatureServer(PORT).start());
        After(() -> temperatureServer.stop());
    }
}
