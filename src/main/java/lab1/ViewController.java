package lab1;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {
    private final AddressBookRepository abRepo;

    public ViewController(AddressBookRepository abRepo) { this.abRepo = abRepo; }

    // Visit: http://localhost:8080/addressbooks/1/view
    @GetMapping("/addressbooks/{id}/view")
    public String viewBook(@PathVariable Long id, Model model) {
        AddressBook book = abRepo.findById(id).orElseThrow();
        model.addAttribute("addressBook", book);
        return "addressbook"; // looks for templates/addressbook.html
    }
}
