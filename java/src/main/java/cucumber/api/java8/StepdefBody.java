package cucumber.api.java8;

public interface StepdefBody {
    @FunctionalInterface
    public static interface A1 extends StepdefBody {
        void accept(String p1);
    }

    @FunctionalInterface
    public static interface A2 extends StepdefBody {
        void accept(String p1, String p2);
    }
}
