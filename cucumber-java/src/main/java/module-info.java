import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.java.JavaBackendProviderService;

module io.cucumber.java {
    requires org.jspecify;

    requires transitive io.cucumber.datatable;
    requires transitive io.cucumber.docstring;
    requires transitive org.apiguardian.api;

    requires io.cucumber.core;
    provides BackendProviderService
            with JavaBackendProviderService;

}
