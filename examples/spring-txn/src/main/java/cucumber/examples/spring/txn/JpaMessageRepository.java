package cucumber.examples.spring.txn;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class JpaMessageRepository implements MessageRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @Override
    public void save(Message message) {
        entityManager.persist(message);
    }
}
