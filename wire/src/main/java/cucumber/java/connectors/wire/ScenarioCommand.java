package cucumber.java.connectors.wire;

import java.util.ArrayList;
import java.util.List;

public abstract class ScenarioCommand implements WireCommand {
    protected List<String> tags;

    public ScenarioCommand(List<String> tags) {
        // Cucumber-Wire passes the tags without the "@" while Cucumber-JVM expects the "@", so add it back on
        if (tags != null && tags.size() > 0) {
            for (String tag : tags) {
                if (this.tags == null) {
                    this.tags = new ArrayList<String>();
                }
                this.tags.add("@" + tag);
            }
        }
    }
}
