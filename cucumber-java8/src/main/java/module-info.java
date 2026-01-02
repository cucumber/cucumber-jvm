import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.java8.Java8BackendProviderService;

module io.cucumber.java8 {
    requires org.jspecify;

    requires transitive io.cucumber.datatable;
    requires transitive io.cucumber.docstring;
    requires transitive org.apiguardian.api;

    requires io.cucumber.core;
    provides BackendProviderService
            with Java8BackendProviderService;

}
