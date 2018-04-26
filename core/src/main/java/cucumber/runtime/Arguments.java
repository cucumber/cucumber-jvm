package cucumber.runtime;

import cucumber.api.Argument;

public final class Arguments {

    private Arguments() {
        //Not for construction
    }

    public static Argument createArgument(final Integer offset, final String val) {
        return new Argument() {
            @Override
            public Integer getOffset() {
                return offset;
            }

            @Override
            public String getVal() {
                return val;
            }

            @Override
            public String toString() {
                return getVal();
            }
        };
    }
}
