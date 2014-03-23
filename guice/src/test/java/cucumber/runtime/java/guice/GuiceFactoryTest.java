package cucumber.runtime.java.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import cucumber.runtime.java.ObjectFactory;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class GuiceFactoryTest {
    @Test
    public void shouldGiveUsNewInstancesForEachScenario() throws IOException {
        ObjectFactory factory = new GuiceFactory();
        factory.addClass(Mappings.class);

        // Scenario 1
        factory.start();
        Mappings o1 = factory.getInstance(Mappings.class);
        factory.stop();

        // Scenario 2
        factory.start();
        Mappings o2 = factory.getInstance(Mappings.class);
        factory.stop();

        assertNotNull(o1);
        assertNotSame(o1, o2);
    }

    @Test
    public void testShouldGiveTheSameInstanceWithinAScenario() throws Exception {
        ObjectFactory factory = new GuiceFactory();
        factory.addClass(Mappings.class);

        factory.start();
        assertSame(factory.getInstance(Mappings.class), factory.getInstance(Mappings.class));
        factory.stop();
    }

    /** Testing a use case of {@link GuiceFactory#createInjector */
    @Test
    public void subclassCanProvideParentInjector() throws Exception {
        // injector can come from anywhere
        final DependencyFromParent dependencyFromParent = new DependencyFromParent();
        final Injector parentInjector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(DependencyFromParent.class).toInstance(dependencyFromParent);
            }
        });

        ObjectFactory factory = new GuiceFactory() {
            @Override
            protected Injector createInjector(Module cucumberModule) {
                return parentInjector.createChildInjector(cucumberModule);
            }
        };
        factory.start();
        assertSame(dependencyFromParent, factory.getInstance(DependencyFromParent.class));
        factory.stop();

        // parent injector is reused
        factory.start();
        assertSame(dependencyFromParent, factory.getInstance(DependencyFromParent.class));
        factory.stop();
    }

    private static class DependencyFromParent {}
}
