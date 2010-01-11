package org.books.business;

import org.books.domain.Book;
import org.books.domain.BookQuery;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class CatalogManagerImpl implements CatalogManager {

    @PersistenceContext(unitName = "bookstore")
    private EntityManager entityManager;

    private String message = "first";

    public List<Book> searchBooks(BookQuery bookQuery) {
        if ((bookQuery.getTitle() + bookQuery.getAuthor() + bookQuery.getPublisher()).length() == 0) {
            return new ArrayList<Book>();
        }
        Query query = entityManager.createNamedQuery("findBooks");
        query.setParameter("title", "%" + bookQuery.getTitle().toLowerCase() + "%");
        query.setParameter("author", "%" + bookQuery.getAuthor().toLowerCase() + "%");
        query.setParameter("publisher", "%" + bookQuery.getPublisher().toLowerCase() + "%");
        return query.getResultList();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
