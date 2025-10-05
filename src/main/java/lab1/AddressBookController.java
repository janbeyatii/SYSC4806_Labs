package lab1;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/addressbooks")
public class AddressBookController {
    private final AddressBookRepository abRepo;
    private final BuddyInfoRepository buddyRepo;

    public AddressBookController(AddressBookRepository abRepo, BuddyInfoRepository buddyRepo) {
        this.abRepo = abRepo;
        this.buddyRepo = buddyRepo;
    }

    // Create an address book
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressBook create(@RequestBody AddressBook ab) {
        return abRepo.save(ab);
    }

    // Get an address book (with buddies)
    @GetMapping("/{id}")
    public AddressBook get(@PathVariable Long id) {
        return abRepo.findById(id).orElseThrow();
    }

    // Add a buddy to an address book: { "name": "John", "phone": "123" }
    @PostMapping("/{id}/buddies")
    public AddressBook addBuddy(@PathVariable Long id, @RequestBody BuddyInfo buddy) {
        AddressBook book = abRepo.findById(id).orElseThrow();
        book.addBuddy(buddy);
        return abRepo.save(book);
    }

    // Bulk add multiple buddies at once
    @PostMapping("/{id}/buddies/bulk")
    public AddressBook addMultipleBuddies(@PathVariable Long id,
                                          @RequestBody List<BuddyInfo> buddies) {
        AddressBook book = abRepo.findById(id).orElseThrow();
        for (BuddyInfo b : buddies) {
            book.addBuddy(b);
        }
        return abRepo.save(book);
    }


    // Remove a buddy by buddyId from an address book
    @DeleteMapping("/{id}/buddies/{buddyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBuddy(@PathVariable Long id, @PathVariable Long buddyId) {
        AddressBook book = abRepo.findById(id).orElseThrow();
        book.removeBuddyById(buddyId);
        abRepo.save(book);
    }

    // delete an entire address book
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable Long id) {
        abRepo.deleteById(id);
    }

    // Simple error mapping
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoSuchElementException.class)
    public String notFound(NoSuchElementException e) { return "Not found"; }
}
