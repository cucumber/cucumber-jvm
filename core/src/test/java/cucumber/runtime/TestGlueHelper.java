package cucumber.runtime;

public class TestGlueHelper implements GlueSupplier {

    private final RuntimeGlue glue = new RuntimeGlue();
    
    @Override
    public Glue get() {
        return glue;
    }
}
