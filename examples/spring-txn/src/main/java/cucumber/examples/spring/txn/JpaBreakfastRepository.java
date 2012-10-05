package cucumber.examples.spring.txn;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class JpaBreakfastRepository implements BreakfastRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @Override
    public void save(List<Breakfast> breakfasts) {
        for (Breakfast breakfast : breakfasts) {
            entityManager.persist(breakfast);
        }
    }

    @Override
    public List<Breakfast> findAll() {
        return entityManager.createQuery("SELECT b FROM Breakfast b", Breakfast.class).getResultList();
    }
}
