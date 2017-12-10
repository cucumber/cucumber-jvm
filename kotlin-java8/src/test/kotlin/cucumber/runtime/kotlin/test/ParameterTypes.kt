package cucumber.runtime.kotlin.test

import cucumber.api.Configuration
import io.cucumber.datatable.DataTableType
import io.cucumber.datatable.TableRowTransformer
import io.cucumber.java.TypeRegistry
import java.util.Locale.ENGLISH

class ParameterTypes : Configuration {

    override fun createTypeRegistry(): TypeRegistry {
        val parameterTypeRegistry = TypeRegistry(ENGLISH)

        parameterTypeRegistry.defineDataTableType(DataTableType(
                "person",
                LambdaStepdefs.Person::class.java,
                TableRowTransformer<LambdaStepdefs.Person>
                { map: Map<String, String> ->
                    val person = LambdaStepdefs.Person()
                    person.first = map.get("first")
                    person.last = map.get("last")
                    person
                }))


        return parameterTypeRegistry
    }
}
