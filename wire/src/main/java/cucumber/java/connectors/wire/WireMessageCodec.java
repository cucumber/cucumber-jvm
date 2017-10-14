package cucumber.java.connectors.wire;

/**
 * Transforms wire messages into commands and responses to messages.
 */
public interface WireMessageCodec {
    /**
     * Decodes a wire message into a command.
     *
     * @param request single message to decode
     *
     * @return The decoded command (ownership passed to the caller)
     */
    WireCommand decode(String request);

    /**
     * Encodes a response to wire format.
     *
     * @param response to encode
     *
     * @return The encoded string
     */
    String encode(WireResponse response);
}
