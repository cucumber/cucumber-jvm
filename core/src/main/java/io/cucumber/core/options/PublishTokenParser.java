package io.cucumber.core.options;

import java.util.regex.Pattern;

public final class PublishTokenParser {

    private PublishTokenParser(){

    }

    public static String parse(String argument) {
        Pattern pattern = Pattern.compile("^[A-Za-z0-9+/=]+$");
        if (argument == null || !pattern.matcher(argument).matches()) {
            throw new IllegalArgumentException("Invalid token. A token must consist of a RFC4648 Base64 encoded string");
        }
        return argument;
    }

}
