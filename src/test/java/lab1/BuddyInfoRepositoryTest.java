package lab1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BuddyInfoRepositoryTest {

    @Autowired
    BuddyInfoRepository buddyRepo;

    @Autowired
    AddressBookRepository addressBookRepo;

    @Test
    @DisplayName("save BuddyInfo standalone; findByName works")
    void saveStandalone_and_findByName() {
        BuddyInfo b = new BuddyInfo("John", "123");
        BuddyInfo saved = buddyRepo.save(b);

        assertThat(saved.getId()).isNotNull();

        Iterable<BuddyInfo> results = buddyRepo.findByName("John");
        assertThat(results).extracting(BuddyInfo::getPhone).contains("123");
    }

    @Test
    @DisplayName("link BuddyInfo to AddressBook and persist FK")
    void linkToAddressBook_and_persist_fk() {
        AddressBook ab = addressBookRepo.save(new AddressBook("Owner"));

        BuddyInfo b = new BuddyInfo("Mary", "555");
        b.setAddressBook(ab);
        buddyRepo.save(b);

        // Verify link via fresh load
        List<BuddyInfo> marys = (List<BuddyInfo>) buddyRepo.findByName("Mary");
        assertThat(marys).hasSize(1);
        BuddyInfo loaded = marys.get(0);
        assertThat(loaded.getAddressBook()).isNotNull();
        assertThat(loaded.getAddressBook().getOwner()).isEqualTo("Owner");
    }

    @Test
    @DisplayName("cascade from AddressBook -> BuddyInfo on save")
    void cascade_from_addressbook() {
        AddressBook ab = new AddressBook("Usman");
        BuddyInfo b1 = new BuddyInfo("A", "1");
        BuddyInfo b2 = new BuddyInfo("B", "2");
        ab.addBuddy(b1);
        ab.addBuddy(b2);

        addressBookRepo.save(ab);

        // buddies should be persisted with ids
        Iterable<BuddyInfo> all = buddyRepo.findAll();
        assertThat(all).hasSize(2);
        assertThat(all).allSatisfy(b -> assertThat(b.getId()).isNotNull());
    }

    @Test
    @DisplayName("orphanRemoval from AddressBook removes BuddyInfo rows")
    void orphanRemoval_works() {
        AddressBook ab = new AddressBook("Owner");
        BuddyInfo b = new BuddyInfo("Orphan", "000");
        ab.addBuddy(b);

        ab = addressBookRepo.save(ab);

        // remove from the collection → orphanRemoval should delete row
        ab.removeBuddy(b);
        addressBookRepo.save(ab);

        assertThat(buddyRepo.findByName("Orphan")).isEmpty();
    }

    @Test
    @DisplayName("delete BuddyInfo directly")
    void delete_buddy_directly() {
        BuddyInfo b = buddyRepo.save(new BuddyInfo("Zed", "999"));
        buddyRepo.delete(b);
        assertThat(buddyRepo.findByName("Zed")).isEmpty();
    }
}
