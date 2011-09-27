package cucumber.runtime.java.guice;

import static java.text.MessageFormat.format;
import static java.util.logging.Level.SEVERE;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.inject.Module;

public class ModuleInstantiator {

    private final Logger logger;

    public ModuleInstantiator() {
        this(Logger.getLogger(ModuleInstantiator.class.getCanonicalName()));
    }

    public ModuleInstantiator(Logger logger) {
        this.logger = logger;
    }

    public List<Module> instantiate(String moduleClassName) {
        try {
            Module module = (Module) Class.forName(moduleClassName).newInstance();
            return Collections.singletonList(module);
        } catch (Exception e) {
            String message = format("Instantiation of ''{0}'' failed", moduleClassName);
            logger.log(SEVERE, message, e);
        }
        return Collections.emptyList();

    }
}