package io.cucumber.java.anonymous;

import java.util.function.Function;

@SuppressWarnings({ "unused", "Convert2Lambda" })
class AnonymousClassHolder {

    Function<String, String> anonymousClass = new Function<>() {
        @Override
        public String apply(String s) {
            return s + "!";
        }
    };
}
