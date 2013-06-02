package cucumber.api.needle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.akquinet.jbosscc.needle.injection.InjectionProvider;

/**
 * Annotation to mark InjectionProviders in the cucumber glue or cucumber steps. <br/>
 * Should be placed on fields of type {@link InjectionProvider} or an array of those.
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface NeedleInjectionProvider {
    // Nothing here
}
