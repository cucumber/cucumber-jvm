package cucumber.runtime;

import java.util.Comparator;

class HookComparator implements Comparator<HookDefinition> {
    private final boolean ascending;

    private HookComparator(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public int compare(HookDefinition hook1, HookDefinition hook2) {
        int comparison = hook1.getOrder() - hook2.getOrder();
        return ascending ? comparison : -comparison;
    }

    // stateless objects - no need for other instances created
    public static final HookComparator ASCENDING = new HookComparator(true);
    public static final HookComparator DESCENDING = new HookComparator(false);
}
