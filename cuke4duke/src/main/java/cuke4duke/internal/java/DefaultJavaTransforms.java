package cuke4duke.internal.java;

import cuke4duke.Transform;

public class DefaultJavaTransforms {

    @Transform
    public int transformStringToInt(String integer) {
        return Integer.valueOf(integer);
    }
    
}
