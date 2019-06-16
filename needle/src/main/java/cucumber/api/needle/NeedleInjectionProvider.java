package cucumber.api.needle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark InjectionProviders in the cucumber glue or cucumber steps.
 * Should be placed on fields of type {@link de.akquinet.jbosscc.needle.injection.InjectionProvider} or an array of those.
 *
 * @deprecated use {@code NeedleInjectionProvider} instead
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface NeedleInjectionProvider {
}
