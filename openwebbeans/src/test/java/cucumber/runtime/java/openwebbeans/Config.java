package cucumber.runtime.java.openwebbeans;

import cucumber.api.openwebbeans.OpenWebBeansConfig;

// not mandatory but allow to start it manually somewhere else
public class Config implements OpenWebBeansConfig {
    @Override
    public boolean userManaged() {
        return false;
    }
}
