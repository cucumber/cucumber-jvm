package gradle.cucumber;

public class Production {

    public void doWork() {
        throw new RuntimeException("production failed here");
    }

}
