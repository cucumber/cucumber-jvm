package cucumber.runtime.transformers;

import java.util.Locale;

public class CharacterTransformer implements Transformer<Character> {

    public Character transform(Locale locale, String... arguments) {
        if (arguments[0].length() < 1) {
            return null;
        }
        return arguments[0].charAt(0);
    }

}
