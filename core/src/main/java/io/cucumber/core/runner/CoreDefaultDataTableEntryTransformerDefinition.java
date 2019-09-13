package io.cucumber.core.runner;

import io.cucumber.core.backend.DefaultDataTableEntryTransformerDefinition;
import io.cucumber.datatable.TableCellByTypeTransformer;
import io.cucumber.datatable.TableEntryByTypeTransformer;

import java.lang.reflect.Type;
import java.util.Map;

class CoreDefaultDataTableEntryTransformerDefinition implements DefaultDataTableEntryTransformerDefinition {

    private final DefaultDataTableEntryTransformerDefinition delegate;
    private final TableEntryByTypeTransformer transformer;

    private CoreDefaultDataTableEntryTransformerDefinition(DefaultDataTableEntryTransformerDefinition delegate) {
        this.delegate = delegate;
        TableEntryByTypeTransformer transformer = delegate.tableEntryByTypeTransformer();
        this.transformer = delegate.headersToProperties() ? new ConvertingTransformer(transformer) : transformer;
    }

    public static CoreDefaultDataTableEntryTransformerDefinition create(DefaultDataTableEntryTransformerDefinition definition) {
        // Ideally we would avoid this by keeping the scenario scoped
        // glue in a different bucket from the globally scoped glue.
        if (definition instanceof ScenarioScoped) {
            return new CoreDefaultDataTableEntryTransformerDefinition.ScenarioCoreDefaultDataTableEntryTransformerDefinition(definition);
        }
        return new CoreDefaultDataTableEntryTransformerDefinition(definition);
    }

    @Override
    public boolean headersToProperties() {
        return delegate.headersToProperties();
    }

    @Override
    public TableEntryByTypeTransformer tableEntryByTypeTransformer() {
        return transformer;
    }

    @Override
    public String getLocation() {
        return delegate.getLocation();
    }

    private static class ScenarioCoreDefaultDataTableEntryTransformerDefinition extends CoreDefaultDataTableEntryTransformerDefinition implements ScenarioScoped {

        private final ScenarioScoped delegate;

        ScenarioCoreDefaultDataTableEntryTransformerDefinition(DefaultDataTableEntryTransformerDefinition delegate) {
            super(delegate);
            this.delegate = (ScenarioScoped) delegate;
        }

        @Override
        public void disposeScenarioScope() {
            delegate.disposeScenarioScope();
        }
    }

    private static class ConvertingTransformer implements TableEntryByTypeTransformer {
        private final CamelCaseStringConverter converter = new CamelCaseStringConverter();
        private final TableEntryByTypeTransformer delegate;

        ConvertingTransformer(TableEntryByTypeTransformer delegate) {
            this.delegate = delegate;
        }

        @Override
        public Object transform(Map<String, String> entryValue, Type toValueType, TableCellByTypeTransformer cellTransformer) throws Throwable {
            return delegate.transform(converter.toCamelCase(entryValue), toValueType, cellTransformer);
        }
    }
}
