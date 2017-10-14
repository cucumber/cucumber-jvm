package cucumber.java.connectors.wire;

import cucumber.java.CucumberJavaWire;
import cucumber.java.CukeEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wire protocol handler, delegating JSON encoding and decoding to a
 * codec object and running commands on a provided engine instance.
 */
public class WireProtocolHandler implements ProtocolHandler {
    private final Logger logger = LoggerFactory.getLogger(CucumberJavaWire.class);

    private WireMessageCodec codec;
    private CukeEngine engine;

    public WireProtocolHandler(WireMessageCodec codec, CukeEngine engine) {
        this.codec = codec;
        this.engine = engine;
    }

    public String handle(String request) throws Throwable {
        String response = "";
        // LOG request
        try {
            WireCommand command = codec.decode(request);
            WireResponse wireResponse = command.run(engine);
            response = codec.encode(wireResponse);
        } catch (Exception e) {
            logger.error("Fatal error occurred", e);
            response = "[\"fail\"]";
        }
        // LOG response
        return response;
    }
}
