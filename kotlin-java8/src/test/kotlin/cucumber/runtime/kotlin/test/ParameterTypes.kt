package cucumber.runtime.kotlin.test

import cucumber.api.Configuration
import cucumber.api.TypeRegistry
import io.cucumber.datatable.DataTableType
import io.cucumber.datatable.TableEntryTransformer
import java.util.Locale.ENGLISH

class ParameterTypes : Configuration {

    override fun createTypeRegistry(): TypeRegistry {
        val typeRegistry = TypeRegistry(ENGLISH)

        typeRegistry.defineDataTableType(DataTableType(
                LambdaStepdefs.Person::class.java,
                TableEntryTransformer<LambdaStepdefs.Person>
                { map: Map<String, String> ->
                    val person = LambdaStepdefs.Person()
                    person.first = map.get("first")
                    person.last = map.get("last")
                    person
                }))


        return typeRegistry
    }
}
