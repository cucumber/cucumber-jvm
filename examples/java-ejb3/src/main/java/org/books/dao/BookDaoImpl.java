package org.books.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.books.domain.Book;

@Stateless
public class BookDaoImpl implements BookDao {

    @PersistenceContext(unitName = "bookstore")
    private EntityManager entityManager;

    public void addBook(Book book) throws Exception {
        entityManager.persist(book);
    }

    public void deleteBook(Book book) throws Exception {
        entityManager.remove(book);
    }

    public List<Book> getBooks() {
        Query query = entityManager.createQuery("SELECT m from Book as m");
        return query.getResultList();
    }


}
