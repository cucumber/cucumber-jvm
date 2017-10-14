package cucumber.java.connectors.wire.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import cucumber.java.StepMatch;
import cucumber.java.StepMatchArg;
import cucumber.java.connectors.wire.StepMatchesResponse;

import java.lang.reflect.Type;
import java.util.List;

public class StepMatchesResponseSerializer implements JsonSerializer<StepMatchesResponse> {
    public JsonElement serialize(
            StepMatchesResponse stepMatchesResponse,
            Type type,
            JsonSerializationContext jsonSerializationContext) {

        JsonArray response = new JsonArray();

        List<StepMatch> matchingSteps = stepMatchesResponse.getMatchingSteps();
        if (matchingSteps != null) {
            for (StepMatch stepMatch : matchingSteps) {
                JsonObject jsonStepMatch = new JsonObject();
                response.add(jsonStepMatch);

                // id
                jsonStepMatch.addProperty("id", stepMatch.getId());

                // args
                JsonArray jsonArgs = new JsonArray();
                jsonStepMatch.add("args", jsonArgs);
                List<StepMatchArg> stepMatchArgs = stepMatch.getArgs();
                if (stepMatchArgs != null) {
                    for (StepMatchArg arg : stepMatchArgs) {
                        JsonObject jsonArg = new JsonObject();
                        jsonArg.addProperty("val", arg.getValue());
                        jsonArg.addProperty("pos", arg.getPosition());
                        jsonArgs.add(jsonArg);
                    }
                }

                if (stepMatch.getSource() != null && stepMatch.getRegexp() != null) {
                    // source
                    jsonStepMatch.addProperty("source", stepMatch.getSource());

                    // regexp
                    jsonStepMatch.addProperty("regexp", stepMatch.getRegexp());
                }
            }
        }

        return response;
    }
}
