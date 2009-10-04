package simple;

import cuke4duke.Transform;

public class Transformations {
    
    @Transform("^'\\d+' to Integer$")
    public Integer stringToInteger(String number) {
        return Integer.valueOf(number);
    }

}
