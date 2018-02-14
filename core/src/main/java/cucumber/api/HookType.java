package cucumber.api;

public enum HookType {
    Before, After, AfterStep;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
