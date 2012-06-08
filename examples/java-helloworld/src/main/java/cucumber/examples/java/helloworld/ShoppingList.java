package cucumber.examples.java.helloworld;

import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class ShoppingList {
    private SortedMap<String,Integer> items = new TreeMap<String, Integer>();

    public void addItem(String name, Integer count) {
        items.put(name, count);
    }

    public void print(Appendable out) throws IOException {
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            out.append(entry.getValue().toString()).append(" ").append(entry.getKey()).append("\n");
        }
    }
}
