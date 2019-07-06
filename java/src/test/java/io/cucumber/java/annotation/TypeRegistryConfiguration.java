package io.cucumber.java.annotation;

import io.cucumber.core.api.TypeRegistry;
import io.cucumber.core.api.TypeRegistryConfigurer;
import io.cucumber.cucumberexpressions.CaptureGroupTransformer;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableEntryTransformer;
import io.cucumber.datatable.TableTransformer;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

import static java.util.Locale.ENGLISH;

public class TypeRegistryConfiguration implements TypeRegistryConfigurer {

    private final TableEntryTransformer<DataTableStepdefs.Author> authorEntryTransformer =
        tableEntry -> new DataTableStepdefs.Author(
            tableEntry.get("firstName"),
            tableEntry.get("lastName"),
            tableEntry.get("birthDate"));

    private final TableTransformer<DataTableStepdefs.Author> singleAuthorTransformer =
        table -> {
            Map<String, String> tableEntry = table.asMaps().get(0);
            return authorEntryTransformer.transform(tableEntry);
        };

    private final CaptureGroupTransformer<LocalDate> localDateParameterType =
        (String[] args) -> LocalDate.of(
            Integer.parseInt(args[0]),
            Integer.parseInt(args[1]),
            Integer.parseInt(args[2])
        );

    @Override
    public Locale locale() {
        return ENGLISH;
    }

    @Override
    public void configureTypeRegistry(TypeRegistry typeRegistry) {
        typeRegistry.defineDataTableType(new DataTableType(
            DataTableStepdefs.Author.class,
            authorEntryTransformer));

        typeRegistry.defineDataTableType(new DataTableType(
            DataTableStepdefs.Author.class,
            singleAuthorTransformer));


        typeRegistry.defineParameterType(new ParameterType<>(
            "parameterTypeRegistryIso8601Date",
            "([0-9]{4})/([0-9]{2})/([0-9]{2})",
            LocalDate.class,
            localDateParameterType
        ));
    }
}
