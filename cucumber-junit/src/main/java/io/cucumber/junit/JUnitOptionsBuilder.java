package io.cucumber.junit;

import org.jspecify.annotations.Nullable;

final class JUnitOptionsBuilder {

    private @Nullable Boolean filenameCompatibleNames = null;
    private @Nullable Boolean stepNotifications = null;

    JUnitOptions build() {
        JUnitOptions jUnitOptions = new JUnitOptions();
        return build(jUnitOptions);
    }

    JUnitOptions build(JUnitOptions jUnitOptions) {
        if (filenameCompatibleNames != null) {
            jUnitOptions.setFilenameCompatibleNames(filenameCompatibleNames);
        }
        if (stepNotifications != null) {
            jUnitOptions.setStepNotifications(stepNotifications);
        }
        return jUnitOptions;
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
