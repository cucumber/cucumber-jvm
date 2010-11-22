package cuke4duke.internal.jvmclass;

import cucumber.annotation.Transform;

public class MyTransforms {

    @Transform
    public boolean overrideBooleanTransform(String yes) {
        if (yes.equals("yes"))
            return true;
        else
            return false;
    }

}