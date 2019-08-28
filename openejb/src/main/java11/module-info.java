module io.cucumber.openejb {
    exports io.cucumber.openejb;
    requires io.cucumber.core;
    requires javaee.api; // FIXME filename-based automodules detected
    requires openejb.core; // FIXME filename-based automodules detected
    requires org.apiguardian.api;
}
