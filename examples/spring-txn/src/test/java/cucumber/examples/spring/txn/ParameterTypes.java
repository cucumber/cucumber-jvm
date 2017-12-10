package cucumber.examples.spring.txn;

import cucumber.api.Configuration;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableRowTransformer;
import io.cucumber.java.TypeRegistry;

import java.util.Map;

import static java.util.Locale.ENGLISH;

public class ParameterTypes implements Configuration {

    @Override
    public TypeRegistry createTypeRegistry() {
        final TypeRegistry parameterTypeRegistry = new TypeRegistry(ENGLISH);

        parameterTypeRegistry.defineDataTableType(new DataTableType(
            "message",
            Message.class,
            new TableRowTransformer<Message>() {
                @Override
                public Message transform(Map<String, String> map) {
                    Message message = new Message();
                    message.setContent(map.get("content"));
                    return message;
                }
            }));

        return parameterTypeRegistry;
    }
}
