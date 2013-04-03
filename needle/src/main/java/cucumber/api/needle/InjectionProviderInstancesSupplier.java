package cucumber.api.needle;

import java.util.Set;

import cucumber.runtime.java.needle.NeedleFactory;
import de.akquinet.jbosscc.needle.NeedleTestcase;
import de.akquinet.jbosscc.needle.injection.InjectionProvider;

/**
 * <a href="http://javadocs.techempower.com/jdk18/api/java/util/function/Supplier.html">Supplies</a> a Set of
 * InjectionProvider instances that are created outside the {@link NeedleFactory} lifecycle.
 */
public interface InjectionProviderInstancesSupplier {

    /**
     * <a href="http://javadocs.techempower.com/jdk18/api/java/util/function/Supplier.html">Supplies</a> a Set of
     * InjectionProvider instances that are created outside the {@link NeedleFactory} lifecycle.
     * 
     * @return InjectionProviders that can be added to {@link NeedleTestcase}
     */
    Set<InjectionProvider<?>> get();
}
