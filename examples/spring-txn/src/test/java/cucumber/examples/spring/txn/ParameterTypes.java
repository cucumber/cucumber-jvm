package cucumber.examples.spring.txn;

import cucumber.api.Configuration;
import cucumber.api.TypeRegistry;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableEntryTransformer;

import java.util.Map;

import static java.util.Locale.ENGLISH;

public class ParameterTypes implements Configuration {

    @Override
    public TypeRegistry createTypeRegistry() {
        final TypeRegistry typeRegistry = new TypeRegistry(ENGLISH);

        typeRegistry.defineDataTableType(new DataTableType(
            Message.class,
            new TableEntryTransformer<Message>() {
                @Override
                public Message transform(Map<String, String> map) {
                    Message message = new Message();
                    message.setContent(map.get("content"));
                    return message;
                }
            }));

        return typeRegistry;
    }
}
