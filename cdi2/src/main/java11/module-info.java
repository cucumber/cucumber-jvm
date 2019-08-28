module io.cucumber.cdi2 {
    exports io.cucumber.cdi2;
    requires cdi.api; // FIXME filename-based automodules detected
    requires io.cucumber.core;
    requires org.apiguardian.api;
}
