package cucumber.java.connectors.wire.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import cucumber.java.connectors.wire.InvokeCommand;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class InvokeCommandDeserializer implements JsonDeserializer<InvokeCommand> {
    public InvokeCommand deserialize(
            JsonElement jsonElement,
            Type type,
            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();

        String stepId = object.getAsJsonPrimitive("id").getAsString();
        List<String> args = new ArrayList<String>();
        List<List<String>> tableArg = new ArrayList<List<String>>();

        JsonArray argsArray = object.getAsJsonArray("args");
        for (JsonElement element : argsArray) {
            if (element instanceof JsonPrimitive) {
                args.add(element.getAsJsonPrimitive().getAsString());
            } else if (element instanceof JsonArray) {
                JsonArray outerArray = element.getAsJsonArray();
                for (JsonElement innerElement : outerArray) {
                    if (innerElement instanceof JsonArray) {
                        JsonArray innerArray = innerElement.getAsJsonArray();
                        List<String> row = new ArrayList<String>();
                        tableArg.add(row);
                        for (JsonElement columnElement : innerArray) {
                            row.add(columnElement.getAsString());
                        }
                    }
                }
            }
        }

        return new InvokeCommand(stepId, args, tableArg);
    }
}
