package cucumber.runtime.java.guice;

import static java.text.MessageFormat.format;

import java.util.Collections;
import java.util.List;

import com.google.inject.Module;

public class ModuleInstantiator {
    public List<Module> instantiate(String moduleClassName) {
        try {
            Module module = (Module) Class.forName(moduleClassName).newInstance();
            return Collections.singletonList(module);
        } catch (Exception e) {
            String message = format("Instantiation of ''{0}'' failed", moduleClassName);
            throw new GuiceModuleInstantiationFailed(message, e);
        }
    }
}