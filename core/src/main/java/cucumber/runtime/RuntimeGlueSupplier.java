package cucumber.runtime;

public final class RuntimeGlueSupplier implements Supplier<Glue> {

    @Override
    public Glue get() {
        return new RuntimeGlue();
    }
}
