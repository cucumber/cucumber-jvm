package simple;

import cuke4duke.Before;

public abstract class SuperSteps {
    protected String b4;

    @Before("@b4,@whatever")
    public void setB4(Object scenario) {
        b4 = "b4 was here";
    }
}
