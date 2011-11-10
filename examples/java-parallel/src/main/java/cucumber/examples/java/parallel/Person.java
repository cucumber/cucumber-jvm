package cucumber.examples.java.parallel;

public class Person {
    private double height;

    public Person(double height) {
        super();
        this.height = height;
    }

    public double getHeight() {
        return this.height;
    }

    public void growBy(double extraHeight) {
        height += extraHeight;
    }
}
