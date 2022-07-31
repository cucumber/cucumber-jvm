package io.cucumber.core.options;

final class BooleanString {

    static boolean parseBoolean(String s) {
        if (s == null) {
            return false;
        }

        if ("true".equalsIgnoreCase(s)) {
            return true;
        } else if ("false".equalsIgnoreCase(s)) {
            return false;
        }

        if ("1".equalsIgnoreCase(s)) {
            return true;
        } else if ("0".equalsIgnoreCase(s)) {
            return false;
        }

        if ("yes".equalsIgnoreCase(s)) {
            return true;
        } else if ("no".equalsIgnoreCase(s)) {
            return false;
        }

        throw new IllegalArgumentException(
            String.format("'%s' Was not a valid boolean value. Please use either 'true' or 'false'.", s));
    }
}
