package cucumber.runtime.kotlin.test

import cucumber.api.Configuration
import cucumber.api.TypeRegistry
import cucumber.api.datatable.DataTableType
import cucumber.api.datatable.TableEntryTransformer
import java.util.Locale.ENGLISH

class ParameterTypes : Configuration {

    override fun createTypeRegistry(): TypeRegistry {
        val parameterTypeRegistry = TypeRegistry(ENGLISH)

        parameterTypeRegistry.defineDataTableType(DataTableType(
                "person",
                LambdaStepdefs.Person::class.java,
                TableEntryTransformer<LambdaStepdefs.Person>
                { map: Map<String, String> ->
                    val person = LambdaStepdefs.Person()
                    person.first = map.get("first")
                    person.last = map.get("last")
                    person
                }))


        return parameterTypeRegistry
    }
}
