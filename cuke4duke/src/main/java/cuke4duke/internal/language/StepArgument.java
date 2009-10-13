package cuke4duke.internal.language;

public class StepArgument {
    private final String val;
    private final int pos;

    public StepArgument(String val, int pos) {
        this.pos = pos;
        this.val = val;
    }

    public String getVal() {
        return val;
    }

    public int getPos() {
        return pos;
    }
}
