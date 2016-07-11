package cucumber.java.connectors.wire;

/**
 * Protocol that reads one command for each input line.
 */
public interface ProtocolHandler {
    String handle(String request) throws Throwable;
}
