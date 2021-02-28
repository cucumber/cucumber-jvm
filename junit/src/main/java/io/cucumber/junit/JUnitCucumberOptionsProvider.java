package io.cucumber.junit;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.options.CucumberOptionsAnnotationParser;
import io.cucumber.core.snippets.SnippetType;

import java.lang.annotation.Annotation;

final class JUnitCucumberOptionsProvider implements CucumberOptionsAnnotationParser.OptionsProvider {

    private static final Logger log = LoggerFactory.getLogger(JUnitCucumberOptionsProvider.class);

    @Override
    public CucumberOptionsAnnotationParser.CucumberOptions getOptions(Class<?> clazz) {
        CucumberOptions annotation = clazz.getAnnotation(CucumberOptions.class);
        if (annotation != null) {
            return new JunitCucumberOptions(annotation);
        }
        warnWhenTestNGCucumberOptionsAreUsed(clazz);
        return null;
    }

    private static void warnWhenTestNGCucumberOptionsAreUsed(Class<?> clazz) {
        for (Annotation clazzAnnotation : clazz.getAnnotations()) {
            String name = clazzAnnotation.annotationType().getName();
            if ("io.cucumber.testng.CucumberOptions".equals(name)) {
                log.warn(() -> "Ignoring options provided by " + name + " on " + clazz.getName() + ". " +
                        "It is recommend to use separate runner classes for JUnit and TestNG.");
            }
        }
    }

    private static class JunitCucumberOptions implements CucumberOptionsAnnotationParser.CucumberOptions {

        private final CucumberOptions annotation;

        JunitCucumberOptions(CucumberOptions annotation) {
            this.annotation = annotation;
        }

        @Override
        public boolean dryRun() {
            return annotation.dryRun();
        }

        @Override
        public boolean strict() {
            return annotation.strict();
        }

        @Override
        public String[] features() {
            return annotation.features();
        }

        @Override
        public String[] glue() {
            return annotation.glue();
        }

        @Override
        public String[] extraGlue() {
            return annotation.extraGlue();
        }

        @Override
        public String tags() {
            return annotation.tags();
        }

        @Override
        public String[] plugin() {
            return annotation.plugin();
        }

        @Override
        public boolean publish() {
            return annotation.publish();
        }

        @Override
        public boolean monochrome() {
            return annotation.monochrome();
        }

        @Override
        public String[] name() {
            return annotation.name();
        }

        @Override
        public SnippetType snippets() {
            switch (annotation.snippets()) {
                case UNDERSCORE:
                    return SnippetType.UNDERSCORE;
                case CAMELCASE:
                    return SnippetType.CAMELCASE;
                default:
                    throw new IllegalArgumentException("" + annotation.snippets());
            }
        }

        @Override
        public Class<? extends ObjectFactory> objectFactory() {
            return (annotation.objectFactory() == NoObjectFactory.class) ? null : annotation.objectFactory();
        }

    }

}
