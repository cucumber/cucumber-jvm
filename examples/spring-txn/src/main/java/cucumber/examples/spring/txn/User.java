package cucumber.examples.spring.txn;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class User {
    @SuppressWarnings("unused")
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String username;

    public void setUsername(String username) {
        this.username = username;
    }
}
