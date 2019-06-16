package cucumber.api.junit;

import org.apiguardian.api.API;
import org.junit.runners.model.InitializationError;

/**
 * @see io.cucumber.junit.Cucumber
 * @deprecated use {@link io.cucumber.junit.Cucumber} instead.
 */
@Deprecated
@API(status = API.Status.MAINTAINED)
public class Cucumber extends io.cucumber.junit.Cucumber {

    public Cucumber(Class clazz) throws InitializationError {
        super(clazz);
    }
}
