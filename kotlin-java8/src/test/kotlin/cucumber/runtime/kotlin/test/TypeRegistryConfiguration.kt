package cucumber.runtime.kotlin.test

import cucumber.api.TypeRegistryConfigurer
import cucumber.api.TypeRegistry
import io.cucumber.datatable.DataTableType
import io.cucumber.datatable.TableEntryTransformer
import java.util.Locale
import java.util.Locale.ENGLISH

class TypeRegistryConfiguration : TypeRegistryConfigurer {

    override fun locale(): Locale {
        return ENGLISH
    }

    override fun configureTypeRegistry(typeRegistry: TypeRegistry) {
        typeRegistry.defineDataTableType(DataTableType(
                LambdaStepdefs.Person::class.java,
                TableEntryTransformer<LambdaStepdefs.Person>
                { map: Map<String, String> ->
                    val person = LambdaStepdefs.Person()
                    person.first = map.get("first")
                    person.last = map.get("last")
                    person
                }))
    }
}
