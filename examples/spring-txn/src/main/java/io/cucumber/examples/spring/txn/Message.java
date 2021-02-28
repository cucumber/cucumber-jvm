package io.cucumber.examples.spring.txn;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import java.io.Serializable;
import java.util.StringJoiner;

@Entity
@Table(name = "messages")
@Access(AccessType.PROPERTY)
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    @Access(AccessType.FIELD)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private User author;

    private String content;

    public Message() {
    }

    public Message(User author, String content) {
        this.author = author;
        this.content = content;
    }

    @ManyToOne(optional = false)
    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    @Basic(optional = false)
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Message.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("content='" + content + "'")
                .toString();
    }

}
