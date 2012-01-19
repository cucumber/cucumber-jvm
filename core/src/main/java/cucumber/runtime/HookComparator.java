package cucumber.runtime;

import java.util.Comparator;

public final class HookComparator implements Comparator<HookDefinition> {
    final boolean ascending;

    public HookComparator(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public int compare(HookDefinition hook1, HookDefinition hook2) {
        int comparison = hook1.getOrder() - hook2.getOrder();
        return ascending ? comparison : -comparison;
    }
}
