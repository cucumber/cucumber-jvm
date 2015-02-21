package cucumber.runtime;

import java.util.Comparator;

class HookComparator implements Comparator<Hook> {
    private final boolean ascending;

    public HookComparator(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public int compare(Hook hook1, Hook hook2) {
        int comparison = hook1.getOrder() - hook2.getOrder();
        return ascending ? comparison : -comparison;
    }
}