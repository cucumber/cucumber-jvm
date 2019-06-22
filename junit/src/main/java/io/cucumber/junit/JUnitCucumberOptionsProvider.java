package io.cucumber.junit;

import cucumber.api.SnippetType;
import io.cucumber.core.options.CucumberOptionsAnnotationParser;

final class JUnitCucumberOptionsProvider implements CucumberOptionsAnnotationParser.OptionsProvider {
    @Override
    public CucumberOptionsAnnotationParser.CucumberOptions getOptions(Class<?> clazz) {
        CucumberOptions annotation = clazz.getAnnotation(CucumberOptions.class);
        if (annotation == null) {
            return null;
        }
        return new JunitCucumberOptions(annotation);
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
        public String[] tags() {
            return annotation.tags();
        }

        @Override
        public String[] plugin() {
            return annotation.plugin();
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
            return annotation.snippets();
        }

        @Override
        public String[] junit() {
            return annotation.junit();
        }
    }
}
