module io.cucumber.needle {
    exports io.cucumber.needle;
    requires io.cucumber.core;
    requires jbosscc.needle; // FIXME filename-based automodules detected
    requires org.apiguardian.api;
    requires slf4j.api; // FIXME filename-based automodules detected
}
