package cucumber.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ListUtils {

    public static <T> List<List<T>> partition(List<T> source, int groups) {
        if (source.isEmpty()) {;
            return Collections.emptyList();
        }

        int actualNumberOfSegments = Math.min(source.size(), groups);
        final List<List<T>> partition = new ArrayList<List<T>>(actualNumberOfSegments);

        for (int i = 0; i < actualNumberOfSegments; i++)
            partition.add(new LinkedList<T>());

        for (int i = 0; i < source.size(); i++) {
            partition.get(i % actualNumberOfSegments).add(source.get(i));
        }

        return partition;
    }
}
