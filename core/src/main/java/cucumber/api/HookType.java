package cucumber.api;

public enum HookType {
    Before, After;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
