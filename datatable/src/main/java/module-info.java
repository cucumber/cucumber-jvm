module io.cucumber.datatable {
    requires org.jspecify;
    requires transitive org.apiguardian.api;
    requires diffutils; // TODO: Don't shade?

    exports io.cucumber.datatable;
}
