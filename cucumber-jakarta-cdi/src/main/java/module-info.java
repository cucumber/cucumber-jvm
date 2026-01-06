import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.jakarta.cdi.CdiJakartaFactory;

module io.cucumber.jakarta.cdi {
    requires org.jspecify;

    requires transitive org.apiguardian.api;

    requires io.cucumber.core;
    requires jakarta.cdi;

    provides ObjectFactory
            with CdiJakartaFactory;

}
