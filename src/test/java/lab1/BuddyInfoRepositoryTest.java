package lab1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop" // clean schema for tests
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // force embedded H2
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
class BuddyInfoRepositoryTest {

    @Autowired
    BuddyInfoRepository buddyRepo;

    @Autowired
    AddressBookRepository addressBookRepo;

    @Test
    @DisplayName("save BuddyInfo standalone; findByName works; 2-arg ctor defaults address")
    void saveStandalone_and_findByName() {
        BuddyInfo b = new BuddyInfo("John", "123"); // address defaults to "N/A"
        BuddyInfo saved = buddyRepo.save(b);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAddress()).isEqualTo("N/A");

        Iterable<BuddyInfo> results = buddyRepo.findByName("John");
        assertThat(results).extracting(BuddyInfo::getPhone).contains("123");
        assertThat(results).extracting(BuddyInfo::getAddress).contains("N/A");
    }

    @Test
    @DisplayName("link BuddyInfo (with explicit address) to AddressBook and persist FK + address")
    void linkToAddressBook_and_persist_fk() {
        AddressBook ab = addressBookRepo.save(new AddressBook("Owner"));

        BuddyInfo b = new BuddyInfo("Mary", "555", "10 King St");
        b.setAddressBook(ab);
        buddyRepo.save(b);

        // Verify link & address via fresh load
        List<BuddyInfo> marys = (List<BuddyInfo>) buddyRepo.findByName("Mary");
        assertThat(marys).hasSize(1);
        BuddyInfo loaded = marys.get(0);
        assertThat(loaded.getAddressBook()).isNotNull();
        assertThat(loaded.getAddressBook().getOwner()).isEqualTo("Owner");
        assertThat(loaded.getAddress()).isEqualTo("10 King St");
    }

    @Test
    @DisplayName("cascade from AddressBook -> BuddyInfo on save (addresses included)")
    void cascade_from_addressbook() {
        AddressBook ab = new AddressBook("Usman");
        BuddyInfo b1 = new BuddyInfo("A", "1", "A St");
        BuddyInfo b2 = new BuddyInfo("B", "2", "B St");
        ab.addBuddy(b1);
        ab.addBuddy(b2);

        addressBookRepo.save(ab);

        // buddies should be persisted with ids and non-blank addresses
        Iterable<BuddyInfo> all = buddyRepo.findAll();
        assertThat(all).hasSize(2);
        assertThat(all).allSatisfy(b -> {
            assertThat(b.getId()).isNotNull();
            assertThat(b.getAddress()).isNotBlank();
        });
    }

    @Test
    @DisplayName("orphanRemoval from AddressBook removes BuddyInfo rows")
    void orphanRemoval_works() {
        AddressBook ab = new AddressBook("Owner");
        BuddyInfo b = new BuddyInfo("Orphan", "000", "Nowhere");
        ab.addBuddy(b);

        ab = addressBookRepo.save(ab);

        // remove from the collection → orphanRemoval should delete row
        ab.removeBuddy(b);
        addressBookRepo.save(ab);

        assertThat(buddyRepo.findByName("Orphan")).isEmpty();
    }

    @Test
    @DisplayName("delete BuddyInfo directly (with address)")
    void delete_buddy_directly() {
        BuddyInfo b = buddyRepo.save(new BuddyInfo("Zed", "999", "Z St"));
        buddyRepo.delete(b);
        assertThat(buddyRepo.findByName("Zed")).isEmpty();
    }
}
