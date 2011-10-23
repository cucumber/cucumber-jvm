package cucumber.runtime.java.guice;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.google.inject.Module;

import cucumber.runtime.java.guice.loadguicemodule.YourModuleClass;

public class ModuleInstantiator_Test {
    private final ModuleInstantiator instantiator = new ModuleInstantiator();
    
    @Test
    public void instantiatesModuleByFullQualifiedName() throws Exception {
        assertThat(instantiate(YourModuleClass.class), is(instanceOf(YourModuleClass.class)));
    }

    @Test(expected=GuiceModuleInstantiationFailed.class)
    public void onNonExistingClass() throws Exception {
        instantiator.instantiate("some.bogus.Class");
    }
    
    @Test(expected=GuiceModuleInstantiationFailed.class)
    public void onClassNotImplementingModule() throws Exception {
        tryToInstantiateAsModule(String.class);
    }
    
    @Test(expected=GuiceModuleInstantiationFailed.class)
    public void onClassWithPrivateConstructor() throws Exception {
        tryToInstantiateAsModule(PrivateConstructor.class);
    }
    
    private Module instantiate(Class<YourModuleClass> moduleClass) {
        return tryToInstantiateAsModule(moduleClass);
    }

    private Module tryToInstantiateAsModule(Class<?> moduleClass) {
        String moduleClassName = moduleClass.getCanonicalName();
        return instantiator.instantiate(moduleClassName).get(0);
    }    
}