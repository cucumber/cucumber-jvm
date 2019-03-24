package io.cucumber.runtime.kotlin.test

import io.cucumber.core.api.TypeRegistryConfigurer
import io.cucumber.core.api.TypeRegistry
import io.cucumber.datatable.DataTableType
import io.cucumber.datatable.TableEntryTransformer
import io.cucumber.kotlin.Person
import java.util.Locale
import java.util.Locale.ENGLISH

class TypeRegistryConfiguration : TypeRegistryConfigurer {

    override fun locale(): Locale {
        return ENGLISH
    }

    override fun configureTypeRegistry(typeRegistry: TypeRegistry) {
        typeRegistry.defineDataTableType(DataTableType(
                Person::class.java,
                TableEntryTransformer<Person>
                { map: Map<String, String> ->
                    Person(map["first"], map["last"])
                }))
    }
}
