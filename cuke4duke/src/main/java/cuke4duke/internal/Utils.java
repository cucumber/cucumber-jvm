package cuke4duke.internal;

public class Utils {
    public static String join(Object[] oa, String sep) {
        String s = "";
        boolean doSep = false;
        for (Object o : oa) {
            if(doSep) {
                s += sep;
            }
            doSep = true;
            s += o;
        }
        return s;
    }
}
