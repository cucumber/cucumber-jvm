package io.cucumber.junit;

class UndefinedThrowable extends Throwable {
    private static final long serialVersionUID = 1L;

    UndefinedThrowable() {
        super("This step is undefined", null, false, false);
    }

    UndefinedThrowable(String stepText) {
        super(String.format("The step \"%s\" is undefined", stepText), null, false, false);
    }
}
