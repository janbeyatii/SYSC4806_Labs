package lab1;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AddressBookIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    private String base() {
        return "http://localhost:" + port + "/addressbooks";
    }

    // ---------- helpers ----------
    private AddressBook createBook(String owner) {
        AddressBook req = new AddressBook(owner);
        ResponseEntity<AddressBook> resp = rest.postForEntity(base(), req, AddressBook.class);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().getId());
        assertEquals(owner, resp.getBody().getOwner());
        return resp.getBody();
    }

    private AddressBook getBook(Long id) {
        ResponseEntity<AddressBook> resp = rest.getForEntity(base() + "/" + id, AddressBook.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        return resp.getBody();
    }

    private AddressBook addBuddy(Long bookId, String name, String phone, String address) {
        BuddyInfo buddy = new BuddyInfo(name, phone, address);
        ResponseEntity<AddressBook> resp =
                rest.postForEntity(base() + "/" + bookId + "/buddies", buddy, AddressBook.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        return resp.getBody();
    }

    // ---------- tests ----------

    @Test
    void create_and_fetch_empty_book() {
        AddressBook created = createBook("Eemaan");
        assertEquals("Eemaan", created.getOwner());
        // Fetch and verify still empty
        AddressBook fetched = getBook(created.getId());
        assertNotNull(fetched.getId());
        assertEquals(0, fetched.getBuddies().size());
    }

    @Test
    void add_buddy_then_list() {
        AddressBook book = createBook("Usman");
        AddressBook afterAdd = addBuddy(book.getId(), "John", "123", "Ottawa");

        // The controller returns the saved book; should now have 1 buddy
        assertEquals(1, afterAdd.getBuddies().size());
        BuddyInfo b = afterAdd.getBuddies().get(0);
        assertEquals("John", b.getName());
        assertEquals("123", b.getPhone());
        assertEquals("Ottawa", b.getAddress());
        assertNotNull(b.getId());

        // GET to double-check persistence
        AddressBook fetched = getBook(book.getId());
        assertEquals(1, fetched.getBuddies().size());
        assertEquals("John", fetched.getBuddies().get(0).getName());
    }

    @Test
    void bulk_add_then_remove_one() {
        AddressBook book = createBook("Owner");

        BuddyInfo[] payload = {
                new BuddyInfo("Alice", "555-1111", "Toronto"),
                new BuddyInfo("Bob",   "555-2222", "Montreal")
        };
        ResponseEntity<AddressBook> bulkResp =
                rest.postForEntity(base() + "/" + book.getId() + "/buddies/bulk", payload, AddressBook.class);
        assertEquals(HttpStatus.OK, bulkResp.getStatusCode());
        AddressBook afterBulk = bulkResp.getBody();
        assertNotNull(afterBulk);
        assertEquals(2, afterBulk.getBuddies().size());

        // Pick Bob's id to remove
        Long bobId = afterBulk.getBuddies().stream()
                .filter(b -> "Bob".equals(b.getName()))
                .findFirst().orElseThrow().getId();

        // DELETE buddy
        ResponseEntity<Void> delResp = rest.exchange(
                base() + "/" + book.getId() + "/buddies/" + bobId,
                HttpMethod.DELETE,
                null,
                Void.class
        );
        assertEquals(HttpStatus.NO_CONTENT, delResp.getStatusCode());

        // Verify only Alice remains
        AddressBook fetched = getBook(book.getId());
        assertEquals(1, fetched.getBuddies().size());
        assertEquals("Alice", fetched.getBuddies().get(0).getName());
    }

    @Test
    void delete_book_then_404_on_get() {
        AddressBook book = createBook("Temp");
        // Delete the book
        ResponseEntity<Void> delResp = rest.exchange(
                base() + "/" + book.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );
        assertEquals(HttpStatus.NO_CONTENT, delResp.getStatusCode());

        // Next GET should be 404 with your error mapping
        ResponseEntity<String> getResp =
                rest.getForEntity(base() + "/" + book.getId(), String.class);
        assertEquals(HttpStatus.NOT_FOUND, getResp.getStatusCode());
        assertTrue(getResp.getBody().contains("Not found"));
    }
}
