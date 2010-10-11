package cuke4duke.internal.language;

import java.util.Locale;

public interface Transformable {
    public <T> T transform(Object argument, Locale locale) throws Throwable;

}
