package cucumber.examples.spring.txn;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Message {
    @SuppressWarnings("unused")
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private User author;

    private String content;

    public void setAuthor(User author) {
        this.author = author;
    }
}
