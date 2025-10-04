package lab1;

import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface AddressBookRepository extends CrudRepository<AddressBook, Long> {
    Optional<AddressBook> findByOwner(String owner);
}
