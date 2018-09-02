package io.cucumber.core.runner;

import io.cucumber.core.backend.HookDefinition;

import java.util.Comparator;

final class HookComparator implements Comparator<HookDefinition> {
    private final boolean ascending;

    HookComparator(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public int compare(HookDefinition hook1, HookDefinition hook2) {
        int x = hook1.getOrder();
        int y = hook2.getOrder();
        return ascending ? Integer.compare(x, y) : Integer.compare(y, x);
    }
}
