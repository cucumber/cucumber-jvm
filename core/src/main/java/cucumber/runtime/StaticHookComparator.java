package cucumber.runtime;

import java.util.Comparator;

public final class StaticHookComparator implements Comparator<StaticHookDefinition> {
    final boolean ascending;

    public StaticHookComparator(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public int compare(StaticHookDefinition hook1, StaticHookDefinition hook2) {
        int comparison = hook1.getOrder() - hook2.getOrder();
        return ascending ? comparison : -comparison;
    }
}
