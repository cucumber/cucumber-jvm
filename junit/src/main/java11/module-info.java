module io.cucumber.junit {
    exports io.cucumber.junit;
    requires io.cucumber.core;
    requires org.apiguardian.api;
    requires transitive junit; // FIXME filename-based automodules detected
}
