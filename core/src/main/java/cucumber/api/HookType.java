package cucumber.api;

public enum HookType {
    Before, After, BeforeStep, AfterStep;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
