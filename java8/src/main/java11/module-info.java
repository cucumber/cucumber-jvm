module io.cucumber.java8 {
    exports io.cucumber.java8;
    requires io.cucumber.core;
    requires io.cucumber.datatable;
    requires org.apiguardian.api;
    requires typetools; // FIXME filename-based automodules detected
}
