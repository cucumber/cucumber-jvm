package cuke4duke.junit;

import cuke4duke.*;
import org.junit.Ignore;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 */
@RunWith(CucumberJunit4Runner.class)
@Feature("/cuke4duke/junit/demo.feature") // Optional, default use the name of the class, and append .feature
@StepDefinitions({CukeFeature.class}) // A list of step definitions to use for this feature
public class CukeFeature {

    @Scenario("3 green cukes")
    // Use the method name (replace _ with space) if a value is not present
    @Tag("aTag")
    public void ThreeGreenCukes() {
    }

    @Scenario("4 green cukes")
    @Tag({"aTag", "otherTag"})
    public void FourGreenCukes() {
    }

    @Scenario("0 yellow cukes")
    @Ignore
    // This scenario should be ignored
    public void ZeroYellowCukes() {
    }

    @Scenario
    public void lots_of_green_cukes() {
    }

}
