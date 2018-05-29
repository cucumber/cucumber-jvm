package cucumber.runtime;

public class TestGlueHelper implements Supplier<Glue> {

    private final RuntimeGlue glue = new RuntimeGlue();
    
    @Override
    public Glue get() {
        return glue;
    }
}
