module io.cucumber.weld {
    exports io.cucumber.weld;
    requires io.cucumber.core;
    requires org.apiguardian.api;
    requires weld.se; // FIXME filename-based automodules detected
}
