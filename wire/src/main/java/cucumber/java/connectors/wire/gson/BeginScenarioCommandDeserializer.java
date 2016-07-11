package cucumber.java.connectors.wire.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import cucumber.java.connectors.wire.BeginScenarioCommand;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BeginScenarioCommandDeserializer implements JsonDeserializer<BeginScenarioCommand> {

    @Override
    public BeginScenarioCommand deserialize(
            JsonElement jsonElement,
            Type type,
            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        List<String> tags = null;
        JsonObject object = jsonElement.getAsJsonObject();
        JsonArray tagsArray = object.getAsJsonArray("tags");
        if (tagsArray != null) {
            tags = new ArrayList<String>();
            for (JsonElement tagElement : tagsArray) {
                if (tagElement instanceof JsonPrimitive) {
                    tags.add(tagElement.getAsJsonPrimitive().getAsString());
                }
            }
        }

        return new BeginScenarioCommand(tags);
    }
}
