import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.guice.GuiceBackendProviderService;
import io.cucumber.guice.GuiceFactory;

module io.cucumber.guice {
    requires org.jspecify;

    requires transitive io.cucumber.datatable;
    requires transitive io.cucumber.docstring;
    requires transitive org.apiguardian.api;

    requires io.cucumber.core;
    requires com.google.guice;

    exports io.cucumber.guice;
    
    provides ObjectFactory with GuiceFactory;
    provides BackendProviderService with GuiceBackendProviderService;

}
