package lab1;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuddyInfoTest {

    // --- constructor validation ---

    @Test
    void twoArgConstructor_rejects_nulls_or_blanks_for_name_and_phone_and_defaults_address() {
        // nulls
        assertThrows(IllegalArgumentException.class, () -> new BuddyInfo(null, "123"));
        assertThrows(IllegalArgumentException.class, () -> new BuddyInfo("John", null));

        // blanks
        assertThrows(IllegalArgumentException.class, () -> new BuddyInfo(" ", "123"));
        assertThrows(IllegalArgumentException.class, () -> new BuddyInfo("John", " "));

        // defaults address to "N/A"
        BuddyInfo b = new BuddyInfo("Jane", "555-0000");
        assertEquals("N/A", b.getAddress());
    }

    @Test
    void threeArgConstructor_rejects_nulls_or_blanks_for_all_fields() {
        // nulls
        assertThrows(IllegalArgumentException.class, () -> new BuddyInfo(null, "123", "Addr"));
        assertThrows(IllegalArgumentException.class, () -> new BuddyInfo("John", null, "Addr"));
        assertThrows(IllegalArgumentException.class, () -> new BuddyInfo("John", "123", null));

        // blanks
        assertThrows(IllegalArgumentException.class, () -> new BuddyInfo(" ", "123", "Addr"));
        assertThrows(IllegalArgumentException.class, () -> new BuddyInfo("John", " ", "Addr"));
        assertThrows(IllegalArgumentException.class, () -> new BuddyInfo("John", "123", " "));
    }

    // --- setter validation ---

    @Test
    void setters_reject_nulls_or_blanks_including_address() {
        BuddyInfo b = new BuddyInfo("John", "123");

        // nulls
        assertThrows(IllegalArgumentException.class, () -> b.setName(null));
        assertThrows(IllegalArgumentException.class, () -> b.setPhone(null));
        assertThrows(IllegalArgumentException.class, () -> b.setAddress(null));

        // blanks
        assertThrows(IllegalArgumentException.class, () -> b.setName(" "));
        assertThrows(IllegalArgumentException.class, () -> b.setPhone(" "));
        assertThrows(IllegalArgumentException.class, () -> b.setAddress(" "));
    }

    @Test
    void address_setter_updates_value() {
        BuddyInfo b = new BuddyInfo("Mary", "555-1111");
        assertEquals("N/A", b.getAddress()); // default from 2-arg ctor
        b.setAddress("123 Maple St");
        assertEquals("123 Maple St", b.getAddress());
    }

    // --- toString ---

    @Test
    void toString_contains_fields_readably_including_address() {
        BuddyInfo b = new BuddyInfo("Mary", "555-1111", "123 Maple St");
        String s = b.toString();
        assertTrue(s.startsWith("BuddyInfo{"), "toString should start with class name");
        assertTrue(s.contains("name='Mary'"), "toString should include name");
        assertTrue(s.contains("phone='555-1111'"), "toString should include phone");
        assertTrue(s.contains("address='123 Maple St'"), "toString should include address");
        // id is null before persistence; don't assert exact formatting to avoid brittleness
    }

    // --- equals / hashCode semantics ---

    @Test
    void equals_and_hashCode_use_id_when_assigned() throws Exception {
        BuddyInfo b1 = new BuddyInfo("A", "1", "Addr");
        BuddyInfo b2 = new BuddyInfo("A", "1", "Addr");

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
        BuddyInfo b = new BuddyInfo("John", "123", "X St");

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
