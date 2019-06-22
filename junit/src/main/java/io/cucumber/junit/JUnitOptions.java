package io.cucumber.junit;

final class JUnitOptions {

    private boolean strict;
    private boolean filenameCompatibleNames = false;
    private boolean stepNotifications = false;

    boolean filenameCompatibleNames() {
        return filenameCompatibleNames;
    }

    boolean stepNotifications() {
        return stepNotifications;
    }

    boolean isStrict() {
        return strict;
    }

    void setStrict(boolean strict) {
        this.strict = strict;
    }

    void setFilenameCompatibleNames(boolean filenameCompatibleNames) {
        this.filenameCompatibleNames = filenameCompatibleNames;
    }

    void setStepNotifications(boolean stepNotifications) {
        this.stepNotifications = stepNotifications;
    }
}
