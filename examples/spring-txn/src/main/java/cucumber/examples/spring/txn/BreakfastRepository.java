package cucumber.examples.spring.txn;

import java.util.List;

public interface BreakfastRepository {
    void save(List<Breakfast> breakfasts);

    List<Breakfast> findAll();
}
