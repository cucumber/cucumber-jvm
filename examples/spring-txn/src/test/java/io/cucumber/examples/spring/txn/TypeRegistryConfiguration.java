package io.cucumber.examples.spring.txn;

import cucumber.api.TypeRegistryConfigurer;
import cucumber.api.TypeRegistry;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableEntryTransformer;

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

    }
}
