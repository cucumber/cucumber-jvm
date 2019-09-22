package io.cucumber.kotlin

import io.cucumber.core.api.TypeRegistry
import io.cucumber.core.api.TypeRegistryConfigurer
import io.cucumber.datatable.DataTableType
import io.cucumber.datatable.TableEntryTransformer
import java.util.*
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
