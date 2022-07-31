package io.cucumber.junit;

import java.util.Map;

final class JUnitOptionsParser {

    JUnitOptionsBuilder parse(Map<String, String> properties) {
        // TODO: Nothing to parse yet. See
        // https://github.com/cucumber/cucumber-jvm/issues/1675
        return new JUnitOptionsBuilder();
    }

    JUnitOptionsBuilder parse(Class<?> clazz) {
        JUnitOptionsBuilder args = new JUnitOptionsBuilder();

        for (Class<?> classWithOptions = clazz; classWithOptions != Object.class; classWithOptions = classWithOptions
                .getSuperclass()) {
            final CucumberOptions options = classWithOptions.getAnnotation(CucumberOptions.class);

            if (options == null) {
                continue;
            }

            if (options.stepNotifications()) {
                args.setStepNotifications(true);
            }
            if (options.useFileNameCompatibleName()) {
                args.setFilenameCompatibleNames(true);
            }

        }
        return args;
    }

}
