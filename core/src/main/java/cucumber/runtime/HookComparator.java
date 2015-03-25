package cucumber.runtime;

import java.util.Comparator;

class HookComparator implements Comparator<HookDefinition> {
    private final boolean ascending;

    public HookComparator(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public int compare(HookDefinition hookDefinition1, HookDefinition hookDefinition2) {
        int comparison = hookDefinition1.getOrder() - hookDefinition2.getOrder();
        return ascending ? comparison : -comparison;
    }
}