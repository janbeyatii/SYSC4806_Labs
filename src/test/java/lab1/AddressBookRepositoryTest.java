package lab1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AddressBookRepositoryTest {

    @Autowired
    AddressBookRepository addressBookRepository;

    @Autowired
    BuddyInfoRepository buddyInfoRepository;

    @Test
    @DisplayName("save(AddressBook) cascades to buddies; findByOwner works")
    void save_cascades_and_findByOwner_works() {
        AddressBook ab = new AddressBook("Usman");
        BuddyInfo b1 = new BuddyInfo("John", "123", "123 crossroads");
        BuddyInfo b2 = new BuddyInfo("Mary", "456", "123 crossroads");
        ab.addBuddy(b1);
        ab.addBuddy(b2);

        AddressBook saved = addressBookRepository.save(ab);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getBuddies()).hasSize(2);
        assertThat(saved.getBuddies()).allSatisfy(b -> assertThat(b.getId()).isNotNull());

        Optional<AddressBook> byOwner = addressBookRepository.findByOwner("Usman");
        assertThat(byOwner).isPresent();
        assertThat(byOwner.get().getBuddies()).extracting(BuddyInfo::getName)
                .containsExactlyInAnyOrder("John", "Mary");
    }

    @Test
    @DisplayName("orphanRemoval removes detached BuddyInfo rows on flush/save")
    void orphanRemoval_removes_buddies() {
        AddressBook ab = new AddressBook("Owner");
        BuddyInfo b = new BuddyInfo("John", "123", "123 crossroads");
        ab.addBuddy(b);
        ab = addressBookRepository.save(ab);

        // remove buddy and persist the change
        ab.removeBuddy(b);
        ab = addressBookRepository.save(ab);

        // the buddy should be gone from the repository too (orphanRemoval)
        Iterable<BuddyInfo> johns = buddyInfoRepository.findByName("John", "123 crossroads");
        assertThat(johns).isEmpty();
    }

    @Test
    @DisplayName("findAll & relationships are loaded (LAZY/EAGER depends on your mapping)")
    void findAll_loads_relationships() {
        AddressBook ab = new AddressBook("A");
        ab.addBuddy(new BuddyInfo("X", "111"));
        addressBookRepository.save(ab);

        var all = addressBookRepository.findAll();
        assertThat(all).hasSize(1);

        AddressBook fetched = all.iterator().next();
        assertThat(fetched.getBuddies()).hasSize(1);
        assertThat(fetched.getBuddies().get(0).getName()).isEqualTo("X");
    }
}
