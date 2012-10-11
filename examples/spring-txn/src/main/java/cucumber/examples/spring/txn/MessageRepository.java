package cucumber.examples.spring.txn;

public interface MessageRepository {
    void save(Message message);
}
