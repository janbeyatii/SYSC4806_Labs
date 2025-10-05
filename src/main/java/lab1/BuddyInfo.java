package lab1;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "buddy_info")
public class BuddyInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String address; // NEW

    /**
     * Many buddies belong to one address book.
     * LAZY fetch to avoid loading the parent unless needed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_book_id")
    @JsonBackReference
    private AddressBook addressBook;

    /** JPA requires a no-arg constructor; protected is safer. */
    protected BuddyInfo() {}

    /** Backwards-compatible: defaults address so existing calls still work. */
    public BuddyInfo(String name, String phone) {
        setName(name);
        setPhone(phone);
        setAddress("N/A");
    }

    /** Preferred constructor including the new address field. */
    public BuddyInfo(String name, String phone, String address) {
        setName(name);
        setPhone(phone);
        setAddress(address);
    }

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name cannot be null/blank");
        }
        this.name = name;
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("phone cannot be null/blank");
        }
        this.phone = phone;
    }

    public String getAddress() { return address; }
    public void setAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("address cannot be null/blank");
        }
        this.address = address;
    }

    public AddressBook getAddressBook() { return addressBook; }
    public void setAddressBook(AddressBook ab) { this.addressBook = ab; }

    @Override
    public String toString() {
        return "BuddyInfo{id=" + id +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                '}';
    }

    /** Optional: define equality by id once assigned. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BuddyInfo)) return false;
        BuddyInfo that = (BuddyInfo) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
