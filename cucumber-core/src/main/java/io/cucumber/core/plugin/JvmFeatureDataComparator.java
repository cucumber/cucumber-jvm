package io.cucumber.core.plugin;

import java.util.Comparator;

class JvmFeatureDataComparator implements Comparator<JvmElementData> {

    @Override
    public int compare(JvmElementData o1, JvmElementData o2) {
        int c = o1.pickle.getUri().compareTo(o2.pickle.getUri());
        if (c != 0) {
            return c;
        }
        return o1.location.getLine().compareTo(o2.location.getLine());
    }
}
