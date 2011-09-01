package cucumber.runtime.transformers;

import java.util.Locale;

public class CharacterTransformer implements Transformer<Character> {

    public Character transform(Locale locale, String string) {
        if (string.length() < 1) {
            return null;
        }
        return string.charAt(0);
    }

}
