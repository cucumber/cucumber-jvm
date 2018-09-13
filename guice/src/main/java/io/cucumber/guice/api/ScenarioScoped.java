package io.cucumber.guice.api;

import com.google.inject.ScopeAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A custom Guice scope annotation that is usually bound to an instance of
 * <code>ScenarioScope</code>.
 */
@Target({ TYPE, METHOD }) @Retention(RUNTIME) @ScopeAnnotation
public @interface ScenarioScoped    {}