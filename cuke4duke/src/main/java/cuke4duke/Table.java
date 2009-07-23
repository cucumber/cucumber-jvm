package cuke4duke;

import java.util.List;
import java.util.Map;

public interface Table {
    public List<Map<String, String>> hashes();
    public void diff(List<List<String>> table);
//    public void diff(List<Map<String, String>> table);
}
