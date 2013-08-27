package cucumber.example.android.cukeulator.test;

import cucumber.api.android.RunWithCucumber;

/**
 * The instrumentation runner will look for any class that is annotated with @RunWithCucumber.
 * The annotation can be used to set specific parameters for cucumber, like glue and features.
 * You could, for example, create multiple run-configurations in your IDE using differently annotated classes.
 * <p/>
 * You can also run all your features (including these) without this class. In that case default values will be used.
 */
@RunWithCucumber(glue = "cucumber.example.android.cukeulator.test", features = "features/operations")
public class RunCukes {
}
