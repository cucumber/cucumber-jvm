package cucumber.runtime;

import io.cucumber.stepexpression.Argument;

public final class Arguments {

    private Arguments() {
        //Not for construction
    }

    public static Argument createArgument(final String val) {
        return new Argument() {
            @Override
            public Object getValue() {
                return val;
            }
        };
    }
}
