package io.cucumber.core.options;

public class BooleanString {
    /**
     * Parses a string into a boolean
     * 
     * @param  s the string to parse
     * @return   true unless s is null, "", "false", "no" or "0".
     */
    public static boolean parseBoolean(String s) {
        return !(s == null || "".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s)
                || "0".equalsIgnoreCase(s));
    }
}
