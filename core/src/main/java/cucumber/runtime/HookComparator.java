package cucumber.runtime;

import java.util.Comparator;

class HookComparator implements Comparator<HookDefinition> {
    private final boolean ascending;

    public HookComparator(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public int compare(HookDefinition hook1, HookDefinition hook2) {
        int x = hook1.getOrder();
        int y = hook2.getOrder();
        // TODO Java7 PR #1147: Inlined Integer.compare. Not available in java 6 yet.
        int comparison = (x < y) ? -1 : ((x == y) ? 0 : 1);
        return ascending ? comparison : -comparison;
    }
}
