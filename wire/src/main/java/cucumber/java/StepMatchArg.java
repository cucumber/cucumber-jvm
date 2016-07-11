package cucumber.java;

public class StepMatchArg {
    private String val;
    private int pos;

    public StepMatchArg(String value, int position) {
        this.val = value;
        this.pos = position;
    }

    public String getValue() {
        return val;
    }

    public void setValue(String value) {
        this.val = value;
    }

    public int getPosition() {
        return pos;
    }

    public void setPosition(int position) {
        this.pos = position;
    }
}
