package cucumber.api.needle;

/**
 * <a href="http://javadocs.techempower.com/jdk18/api/java/util/function/Supplier.html">Supplies</a> a Set of
 * InjectionProvider instances that are created outside the {@link io.cucumber.needle.NeedleFactory} lifecycle.
 * @deprecated use {@code io.cucumber.needle.api.InjectionProviderInstancesSupplier} instead
 */
@Deprecated
public interface InjectionProviderInstancesSupplier extends io.cucumber.needle.InjectionProviderInstancesSupplier {

}
