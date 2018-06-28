package cucumber.runtime;

public final class RuntimeGlueSupplier implements GlueSupplier {

    @Override
    public Glue get() {
        return new RuntimeGlue();
    }
}
