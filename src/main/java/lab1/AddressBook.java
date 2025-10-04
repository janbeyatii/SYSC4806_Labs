package lab1;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "address_book")
public class AddressBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String owner;

    @OneToMany(
            mappedBy = "addressBook",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JsonManagedReference
    private List<BuddyInfo> buddies = new ArrayList<>();

    /** JPA requires a no-args constructor. */
    protected AddressBook() {}

    public AddressBook(String owner) {
        setOwner(owner);
    }

    public Long getId() { return id; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) {
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("owner cannot be null/blank");
        }
        this.owner = owner;
    }

    public List<BuddyInfo> getBuddies() { return buddies; }

    /** Keep both sides of the relationship in sync. */
    public void addBuddy(BuddyInfo buddy) {
        if (buddy == null) throw new IllegalArgumentException("buddy cannot be null");
        if (!buddies.contains(buddy)) {
            buddies.add(buddy);
            buddy.setAddressBook(this);
        }
    }

    public void removeBuddy(BuddyInfo buddy) {
        if (buddy == null) return;
        if (buddies.remove(buddy)) {
            buddy.setAddressBook(null);
        }
    }

    /** Convenience: remove a buddy by its id. */
    public void removeBuddyById(Long buddyId) {
        if (buddyId == null) return;
        buddies.removeIf(b -> {
            boolean match = buddyId.equals(b.getId());
            if (match) b.setAddressBook(null);
            return match;
        });
    }

    public int size() { return buddies.size(); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AddressBook id=" + id + " owner=" + owner + ":\n");
        for (BuddyInfo b : buddies) sb.append(" - ").append(b).append('\n');
        return sb.toString();
    }

    /** Entities should define equality by primary key once assigned. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AddressBook)) return false;
        AddressBook that = (AddressBook) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return 31; // stable for transient entities; OK once id assigned
    }
}
