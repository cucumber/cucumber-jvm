import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.jakarta.openejb.OpenEJBObjectFactory;

module io.cucumber.jakarta.cdi {
    requires org.jspecify;

    requires io.cucumber.core;
    requires jakartaee.api;
    requires java.naming;
    requires org.apache.tomee.container.core;

    requires transitive io.cucumber.datatable;
    requires transitive io.cucumber.docstring;
    requires transitive org.apiguardian.api;

    provides ObjectFactory
            with OpenEJBObjectFactory;

}
