package io.cucumber.core.options;

import io.cucumber.core.snippets.SnippetType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface CucumberOptions {

    boolean dryRun() default false;

    boolean strict() default true;

    String[] features() default {};

    String[] glue() default {};

    String[] extraGlue() default {};

    String tags() default "";

    String[] plugin() default {};

    boolean publish() default false;

    boolean monochrome() default false;

    String[] name() default {};

    SnippetType snippets() default SnippetType.UNDERSCORE;

    Class<? extends io.cucumber.core.backend.ObjectFactory> objectFactory() default NoObjectFactory.class;

    String[] junit() default {};

}
