import io.cucumber.cdi2.Cdi2Factory;
import io.cucumber.core.backend.ObjectFactory;

@SuppressWarnings("module") module io.cucumber.cdi2 {
    requires org.jspecify;

    requires transitive io.cucumber.datatable;
    requires transitive io.cucumber.docstring;
    requires transitive org.apiguardian.api;

    requires io.cucumber.core;
    requires cdi.api;

    provides ObjectFactory
            with Cdi2Factory;

}
