package cucumber.runtime.java.guice;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import com.google.inject.Module;

import cucumber.runtime.java.guice.loadguicemodule.YourModuleClass;

public class ModuleInstantiator_Test {
    private final Logger logger = mock(Logger.class);
    private final ModuleInstantiator instantiator = new ModuleInstantiator(logger);
    
    @Test
    public void instantiatesModuleByFullQualifiedName() throws Exception {
        Module module  = instantiator.instantiate("cucumber.runtime.java.guice.loadguicemodule.YourModuleClass").get(0);
        assertThat(module, is(instanceOf(YourModuleClass.class)));
    }
    
    @Test
    public void returnsAnEmptyListOnNonExistingClass() throws Exception {
        assertThat(instantiator.instantiate("some.bogus.Class").isEmpty(), is(true));
    }
    
    @Test
    public void returnsAnEmptyListOnClassNotImplementingModule() throws Exception {
        assertThat(instantiator.instantiate(String.class.getCanonicalName()).isEmpty(), is(true));
    }
    
    @Test
    public void returnsAnEmptyListOnClassWithPrivateConstructor() throws Exception {
        assertThat(instantiator.instantiate(PrivateConstructor.class.getCanonicalName()).isEmpty(), is(true));
    }
    
    @Test
    public void logsFailingInstantiation() throws Exception {
        instantiator.instantiate("some.bogus.Class");
        verify(logger).log(eq(Level.SEVERE), contains("Instantiation of 'some.bogus.Class' failed"), any(Throwable.class));
    }
}