package cucumber.runtime.java.guice;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.google.inject.Module;

import cucumber.runtime.java.guice.loadguicemodule.YourModuleClass;

public class ModuleInstantiatorTest {
    private final ModuleInstantiator instantiator = new ModuleInstantiator();
    
    @Test
    public void instantiatesModuleByFullyQualifiedName() throws Exception {
        assertThat(instantiate(YourModuleClass.class), is(instanceOf(YourModuleClass.class)));
    }

    @Test(expected=GuiceModuleInstantiationFailed.class)
    public void fails_to_instantiate_non_existant_class() throws Exception {
        instantiator.instantiate("some.bogus.Class");
    }
    
    @Test(expected=GuiceModuleInstantiationFailed.class)
    public void fails_to_instantiate_class_not_implementing_module() throws Exception {
        instantiate(String.class);
    }
    
    @Test(expected=GuiceModuleInstantiationFailed.class)
    public void fails_to_instantiate_class_with_private_constructor() throws Exception {
        instantiate(PrivateConstructor.class);
    }

    private Module instantiate(Class<?> moduleClass) {
        String moduleClassName = moduleClass.getCanonicalName();
        return instantiator.instantiate(moduleClassName).get(0);
    }    
}