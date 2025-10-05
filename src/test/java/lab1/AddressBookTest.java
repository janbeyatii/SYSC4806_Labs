package lab1;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddressBookTest {

    @Test
    void constructor_rejects_blank_owner() {
        assertThrows(IllegalArgumentException.class, () -> new AddressBook(null));
        assertThrows(IllegalArgumentException.class, () -> new AddressBook(""));
        assertThrows(IllegalArgumentException.class, () -> new AddressBook("   "));
    }

    @Test
    void setOwner_rejects_blank_owner() {
        AddressBook ab = new AddressBook("Alice");
        assertThrows(IllegalArgumentException.class, () -> ab.setOwner(null));
        assertThrows(IllegalArgumentException.class, () -> ab.setOwner(" "));
    }

    @Test
    void addBuddy_adds_and_links_both_sides() {
        AddressBook ab = new AddressBook("Alice");
        BuddyInfo b = new BuddyInfo("John", "123", "123 crossroads");

        ab.addBuddy(b);

        assertEquals(1, ab.size());
        assertTrue(ab.getBuddies().contains(b));
        assertEquals(ab, b.getAddressBook());
    }

    @Test
    void addBuddy_ignores_duplicates_by_identity() {
        AddressBook ab = new AddressBook("Alice");
        BuddyInfo b = new BuddyInfo("John", "123", "123 crossroads");

        ab.addBuddy(b);
        ab.addBuddy(b); // same instance again

        assertEquals(1, ab.size());
    }

    @Test
    void removeBuddy_unlinks_both_sides() {
        AddressBook ab = new AddressBook("Alice");
        BuddyInfo b = new BuddyInfo("John", "123", "123 crossroads");
        ab.addBuddy(b);

        ab.removeBuddy(b);

        assertEquals(0, ab.size());
        assertNull(b.getAddressBook());
    }

    @Test
    void equals_is_based_on_id_once_assigned_otherwise_reference() {
        AddressBook a1 = new AddressBook("Alice");
        AddressBook a2 = new AddressBook("Alice");

        // transient (no id assigned) → not equal unless same instance
        assertNotEquals(a1, a2);
        assertEquals(a1, a1);

        // simulate id assignment to check equals contract
        // (normally the persistence provider sets this)
        TestIds.assignId(a1, 1L);
        TestIds.assignId(a2, 1L);
        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    // small helper to force-set id via reflection for equals/hashCode testing
    static class TestIds {
        static void assignId(AddressBook ab, Long id) {
            try {
                var f = AddressBook.class.getDeclaredField("id");
                f.setAccessible(true);
                f.set(ab, id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
