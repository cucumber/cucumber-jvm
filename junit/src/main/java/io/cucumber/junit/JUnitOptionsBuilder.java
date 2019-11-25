package io.cucumber.junit;

final class JUnitOptionsBuilder {

    private Boolean strict = null;
    private Boolean filenameCompatibleNames = null;
    private Boolean stepNotifications = null;

    JUnitOptions build() {
        JUnitOptions jUnitOptions = new JUnitOptions();
        return build(jUnitOptions);
    }

    JUnitOptions build(JUnitOptions jUnitOptions) {
        if (strict != null) {
            jUnitOptions.setStrict(strict);
        }
        if (filenameCompatibleNames != null) {
            jUnitOptions.setFilenameCompatibleNames(filenameCompatibleNames);
        }
        if (stepNotifications != null) {
            jUnitOptions.setStepNotifications(stepNotifications);
        }
        return jUnitOptions;
    }

    JUnitOptionsBuilder setStrict(boolean strict) {
        this.strict = strict;
        return this;
    }

    JUnitOptionsBuilder setFilenameCompatibleNames(boolean filenameCompatibleNames) {
        this.filenameCompatibleNames = filenameCompatibleNames;
        return this;
    }

    JUnitOptionsBuilder setStepNotifications(boolean stepNotifications) {
        this.stepNotifications = stepNotifications;
        return this;
    }
}
