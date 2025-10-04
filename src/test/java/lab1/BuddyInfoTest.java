package lab1;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuddyInfoTest {

    // --- constructor validation ---

    @Test
    void constructor_rejects_nulls_or_blanks() {
        // nulls
        assertThrows(IllegalArgumentException.class, () -> new BuddyInfo(null, "123"));
        assertThrows(IllegalArgumentException.class, () -> new BuddyInfo("John", null));

        // blanks
        assertThrows(IllegalArgumentException.class, () -> new BuddyInfo(" ", "123"));
        assertThrows(IllegalArgumentException.class, () -> new BuddyInfo("John", " "));
    }

    // --- setter validation ---

    @Test
    void setters_reject_nulls_or_blanks() {
        BuddyInfo b = new BuddyInfo("John", "123");

        // nulls
        assertThrows(IllegalArgumentException.class, () -> b.setName(null));
        assertThrows(IllegalArgumentException.class, () -> b.setPhone(null));

        // blanks
        assertThrows(IllegalArgumentException.class, () -> b.setName(" "));
        assertThrows(IllegalArgumentException.class, () -> b.setPhone(" "));
    }

    // --- toString ---

    @Test
    void toString_contains_fields_readably() {
        BuddyInfo b = new BuddyInfo("Mary", "555-1111");
        String s = b.toString();
        assertTrue(s.startsWith("BuddyInfo{"), "toString should start with class name");
        assertTrue(s.contains("name='Mary'"), "toString should include name");
        assertTrue(s.contains("phone='555-1111'"), "toString should include phone");
        // id is null before persistence; don't assert exact formatting to avoid brittleness
    }

    // --- equals / hashCode semantics ---

    @Test
    void equals_and_hashCode_use_id_when_assigned() throws Exception {
        BuddyInfo b1 = new BuddyInfo("A", "1");
        BuddyInfo b2 = new BuddyInfo("A", "1");

        // Transient entities (no ids) are not equal
        assertNotEquals(b1, b2);
        assertEquals(b1, b1);
        assertEquals(31, b1.hashCode());

        // Simulate JPA assigning same id to both
        setId(b1, 10L);
        setId(b2, 10L);

        assertEquals(b1, b2);
        assertEquals(b1.hashCode(), b2.hashCode());
    }

    // --- back-reference link ---

    @Test
    void addressBook_link_can_be_set_and_cleared() {
        AddressBook ab = new AddressBook("Owner");
        BuddyInfo b = new BuddyInfo("John", "123");

        // link
        b.setAddressBook(ab);
        assertEquals(ab, b.getAddressBook());

        // unlink
        b.setAddressBook(null);
        assertNull(b.getAddressBook());
    }

    // helper to simulate JPA id assignment
    private static void setId(BuddyInfo b, Long id) throws Exception {
        var f = BuddyInfo.class.getDeclaredField("id");
        f.setAccessible(true);
        f.set(b, id);
    }
}
