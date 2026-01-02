import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.jakarta.openejb.OpenEJBObjectFactory;

module io.cucumber.jakarta.cdi {
    requires org.jspecify;

    requires transitive io.cucumber.datatable;
    requires transitive io.cucumber.docstring;
    requires transitive org.apiguardian.api;

    requires io.cucumber.core;
    provides ObjectFactory
            with OpenEJBObjectFactory;

}
