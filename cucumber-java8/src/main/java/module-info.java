import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.java8.Java8BackendProviderService;

@SuppressWarnings("module") // package is called java8
module io.cucumber.java8 {
    requires org.jspecify;

    requires transitive io.cucumber.datatable;
    requires transitive io.cucumber.docstring;
    requires transitive org.apiguardian.api;

    requires io.cucumber.core;
    requires net.jodah.typetools;

    exports io.cucumber.java8;
    
    provides BackendProviderService
            with Java8BackendProviderService;

}
