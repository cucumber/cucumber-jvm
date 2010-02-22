package simple;

import cuke4duke.annotation.Before;

public abstract class SuperSteps {
    protected String b4AndForever = "notSet";
    protected String b4 = "notSet";

    @Before({"@b4,@whatever", "@never-used-either"})
    public void setB4AndForever(Object scenario) {
        b4AndForever = "b4AndForever";
    }

    @Before("@b4")
    public void setB4(Object scenario) {
        b4 = "b4";
    }
}
