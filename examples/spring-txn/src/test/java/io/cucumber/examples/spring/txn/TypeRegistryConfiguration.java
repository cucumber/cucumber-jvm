package io.cucumber.examples.spring.txn;

import io.cucumber.core.api.TypeRegistryConfigurer;
import io.cucumber.core.api.TypeRegistry;
import io.cucumber.datatable.TableCellByTypeTransformer;
import io.cucumber.datatable.TableEntryByTypeTransformer;
import io.cucumber.datatable.dependency.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Locale;
import java.util.Map;

import static java.util.Locale.ENGLISH;

public class TypeRegistryConfiguration implements TypeRegistryConfigurer {

    @Override
    public Locale locale() {
        return ENGLISH;
    }

    @Override
    public void configureTypeRegistry(TypeRegistry typeRegistry) {
        DefaultTransformer defaultTransformer = new DefaultTransformer();
        typeRegistry.setDefaultDataTableCellTransformer(defaultTransformer);
        typeRegistry.setDefaultDataTableEntryTransformer(defaultTransformer);

    }

    private class DefaultTransformer implements TableCellByTypeTransformer, TableEntryByTypeTransformer {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public <T> T transform(String s, Class<T> aClass) {
            return objectMapper.convertValue(s, aClass);
        }

        @Override
        public <T> T transform(Map<String, String> map, Class<T> aClass, TableCellByTypeTransformer tableCellByTypeTransformer) {
            return objectMapper.convertValue(map, aClass);
        }
    }
}
