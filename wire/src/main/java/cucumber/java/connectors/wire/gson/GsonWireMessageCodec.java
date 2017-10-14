package cucumber.java.connectors.wire.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import cucumber.java.CucumberJavaWire;
import cucumber.java.connectors.wire.BeginScenarioCommand;
import cucumber.java.connectors.wire.EndScenarioCommand;
import cucumber.java.connectors.wire.FailingCommand;
import cucumber.java.connectors.wire.FailureResponse;
import cucumber.java.connectors.wire.InvokeCommand;
import cucumber.java.connectors.wire.PendingResponse;
import cucumber.java.connectors.wire.SnippetTextCommand;
import cucumber.java.connectors.wire.SnippetTextResponse;
import cucumber.java.connectors.wire.StepMatchesResponse;
import cucumber.java.connectors.wire.SuccessResponse;
import cucumber.java.connectors.wire.WireCommand;
import cucumber.java.connectors.wire.WireMessageCodec;
import cucumber.java.connectors.wire.StepMatchesCommand;
import cucumber.java.connectors.wire.WireResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WireMessageCodec implementation with Gson.
 */
public class GsonWireMessageCodec implements WireMessageCodec {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public WireCommand decode(String request) {
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(InvokeCommand.class, new InvokeCommandDeserializer());
            gsonBuilder.registerTypeAdapter(BeginScenarioCommand.class, new BeginScenarioCommandDeserializer());
            Gson gson = gsonBuilder.create();
            JsonParser parser = new JsonParser();
            JsonArray array = parser.parse(request).getAsJsonArray();

            if (array.size() > 0) {
                String command = gson.fromJson(array.get(0), String.class);

                if ("begin_scenario".equals(command)) {
                    return gson.fromJson(getSecondObject(array), BeginScenarioCommand.class);
                } else if ("end_scenario".equals(command)) {
                    return gson.fromJson(getSecondObject(array), EndScenarioCommand.class);
                } else if ("step_matches".equals(command)) {
                    return gson.fromJson(getSecondObject(array), StepMatchesCommand.class);
                } else if ("invoke".equals(command)) {
                    return gson.fromJson(getSecondObject(array), InvokeCommand.class);
                } else if ("snippet_text".equals(command)) {
                    return gson.fromJson(getSecondObject(array), SnippetTextCommand.class);
                }
            }
        } catch (JsonSyntaxException e) {
            logger.error("Failed to decode wire command", e);
            // Fail below...
        }

        return new FailingCommand();
    }

    private JsonElement getSecondObject(JsonArray array) {
        if (array.size() > 1) {
            return array.get(1);
        }

        return new JsonObject();
    }

    public String encode(WireResponse response) {
        Object[] array = null;
        GsonBuilder gsonBuilder = new GsonBuilder();

        if (response instanceof FailureResponse) {
            array = new Object[] { "fail", response };
        } else if (response instanceof SuccessResponse) {
            array = new Object[] { "success" };
        } else if (response instanceof PendingResponse) {
            array = new Object[] { "pending", response };
        } else if (response instanceof SnippetTextResponse) {
            array = new Object[] { "success", response };
        } else if (response instanceof StepMatchesResponse) {
            gsonBuilder.registerTypeAdapter(StepMatchesResponse.class, new StepMatchesResponseSerializer());
            array = new Object[] { "success", response };
        }

        if (array != null) {
            try {
                Gson gson = gsonBuilder.create();
                return gson.toJson(array);
            } catch (JsonSyntaxException e) {
                logger.error("Failed to encode wire response", e);
                // Fail below...
            }
        }

        return "[\"fail\"]";
    }
}
