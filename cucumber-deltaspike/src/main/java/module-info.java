import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.deltaspike.DeltaSpikeObjectFactory;

module io.cucumber.deltaspike {
    requires org.jspecify;

    requires transitive io.cucumber.datatable;
    requires transitive io.cucumber.docstring;
    requires transitive org.apiguardian.api;

    requires io.cucumber.core;
    requires deltaspike.cdictrl.api;
    requires cdi.api;

    provides ObjectFactory
            with DeltaSpikeObjectFactory;

}
