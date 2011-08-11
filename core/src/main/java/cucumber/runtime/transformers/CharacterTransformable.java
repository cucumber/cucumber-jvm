package cucumber.runtime.transformers;

import java.util.Locale;

public class CharacterTransformable implements Transformable<Character> {

    public Character transform(String argument, Locale locale) {
        if (argument.length() < 1) {
            return null;
        }
        return Character.valueOf(argument.charAt(0));
    }

}
