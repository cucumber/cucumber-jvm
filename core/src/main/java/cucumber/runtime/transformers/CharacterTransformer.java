package cucumber.runtime.transformers;

import java.util.Locale;

public class CharacterTransformer implements Transformer<Character> {

    public Character transform(String argument, Locale locale) {
        if (argument.length() < 1) {
            return null;
        }
        return argument.charAt(0);
    }

}
