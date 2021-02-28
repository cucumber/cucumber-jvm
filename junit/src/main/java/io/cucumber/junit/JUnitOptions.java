package io.cucumber.junit;

final class JUnitOptions {

    private boolean filenameCompatibleNames = false;
    private boolean stepNotifications = false;

    boolean filenameCompatibleNames() {
        return filenameCompatibleNames;
    }

    boolean stepNotifications() {
        return stepNotifications;
    }

    void setFilenameCompatibleNames(boolean filenameCompatibleNames) {
        this.filenameCompatibleNames = filenameCompatibleNames;
    }

    void setStepNotifications(boolean stepNotifications) {
        this.stepNotifications = stepNotifications;
    }

}
