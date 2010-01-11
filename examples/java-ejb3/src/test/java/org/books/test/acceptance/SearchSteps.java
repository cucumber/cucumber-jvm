package org.books.test.acceptance;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.ejb.EJB;
import javax.naming.NamingException;

import org.apache.openejb.api.LocalClient;
import org.books.business.CatalogManager;
import org.books.dao.BookDao;
import org.books.domain.Book;
import org.books.domain.BookQuery;

import cuke4duke.After;
import cuke4duke.Before;
import cuke4duke.Given;
import cuke4duke.Then;
import cuke4duke.When;


@LocalClient
public class SearchSteps extends ContainerSteps {

	@EJB private BookDao bookDao;
	@EJB private CatalogManager catalogManager;
	
	public SearchSteps(ContainerInitializer initializer) throws NamingException{
		super(initializer);
	}

	private BookQuery bookQuery;
    private List<Book> foundBooks;

    @Before
    public void init(){
    	bookQuery = new BookQuery();
    }
    
    @After
    public void shutdown() throws NamingException {
        context.close();
    }

    @Given("^the following books$")
    public void theFollowingBooksWithTable(cuke4duke.Table table) throws Exception {
        for (List<String> row : table.rows()) {
        	bookDao.addBook(new Book(row.get(0), row.get(1), Integer.parseInt((row.get(2))), row.get(3)));
        }
    }

    @When("^I search for author '(.*)'$")
    public void searchForAuthor(String author) {
    	bookQuery.setAuthor(author);
    }
    
    @When("^I search for title '(.*)'$")
    public void searchForTitle(String title) {
    	bookQuery.setTitle(title);
    }
    
    @When("^I search for publisher '(.*)'$")
    public void searchForPublisher(String publisher) {
    	bookQuery.setPublisher(publisher);
    }

    @Then("^the result list should contain (.*) books?$")
    public void checkSearchResultCount(int count) throws Exception {
        foundBooks = catalogManager.searchBooks(bookQuery);
        
        assertEquals("Result contains wrong count.", count, foundBooks.size());
    }
}
