package lab1;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AddressBookController.class)
class AddressBookControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    AddressBookRepository abRepo;

    @MockBean
    BuddyInfoRepository buddyRepo;

    // ---------- CREATE /addressbooks (POST) ----------
    @Test
    void createAddressBook_returns201_withBody() throws Exception {
        AddressBook request = new AddressBook("Usman");
        AddressBook saved = new AddressBook("Usman");
        setId(saved, 1L);

        when(abRepo.save(any(AddressBook.class))).thenReturn(saved);

        mvc.perform(post("/addressbooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.owner", is("Usman")));

        verify(abRepo).save(any(AddressBook.class));
    }

    // ---------- GET /addressbooks/{id} ----------
    @Test
    void getAddressBook_byId_returns200_withBuddies() throws Exception {
        AddressBook ab = new AddressBook("Usman");
        setId(ab, 1L);
        BuddyInfo b1 = new BuddyInfo("John", "123");
        BuddyInfo b2 = new BuddyInfo("Mary", "555");
        ab.addBuddy(b1);
        ab.addBuddy(b2);
        setId(b1, 10L);
        setId(b2, 11L);

        when(abRepo.findById(1L)).thenReturn(Optional.of(ab));

        mvc.perform(get("/addressbooks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.owner", is("Usman")))
                .andExpect(jsonPath("$.buddies", hasSize(2)))
                .andExpect(jsonPath("$.buddies[*].name", containsInAnyOrder("John", "Mary")))
                .andExpect(jsonPath("$.buddies[*].phone", containsInAnyOrder("123", "555")));
    }

    @Test
    void getAddressBook_notFound_mapsTo404() throws Exception {
        when(abRepo.findById(99L)).thenReturn(Optional.empty());

        mvc.perform(get("/addressbooks/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Not found")));
    }

    // ---------- POST /addressbooks/{id}/buddies (single) ----------
    @Test
    void addBuddy_toAddressBook_returns200_andPersists() throws Exception {
        AddressBook ab = new AddressBook("Owner");
        setId(ab, 1L);
        when(abRepo.findById(1L)).thenReturn(Optional.of(ab));

        BuddyInfo req = new BuddyInfo("John", "123", "123 crossroads");
        AddressBook saved = new AddressBook("Owner");
        setId(saved, 1L);
        // after save, it should contain the buddy
        saved.addBuddy(cloneWithId(new BuddyInfo("John", "123", "123 crossroads"), 10L));

        ArgumentCaptor<AddressBook> abCaptor = ArgumentCaptor.forClass(AddressBook.class);
        when(abRepo.save(abCaptor.capture())).thenReturn(saved);

        mvc.perform(post("/addressbooks/1/buddies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.buddies", hasSize(1)))
                .andExpect(jsonPath("$.buddies[0].name", is("John")))
                .andExpect(jsonPath("$.buddies[0].phone", is("123")));

        // Verify controller linked buddy to book before saving
        AddressBook toSave = abCaptor.getValue();
        // there should be exactly one buddy linked to that instance
        org.junit.jupiter.api.Assertions.assertEquals(1, toSave.getBuddies().size());
        org.junit.jupiter.api.Assertions.assertSame(toSave, toSave.getBuddies().get(0).getAddressBook());
    }

    // ---------- POST /addressbooks/{id}/buddies/bulk ----------
    @Test
    void bulkAddBuddies_returns200_withAllAdded() throws Exception {
        AddressBook ab = new AddressBook("Owner");
        setId(ab, 1L);
        when(abRepo.findById(1L)).thenReturn(Optional.of(ab));

        List<BuddyInfo> payload = List.of(
                new BuddyInfo("A", "1"),
                new BuddyInfo("B", "2"),
                new BuddyInfo("C", "3")
        );

        AddressBook saved = new AddressBook("Owner");
        setId(saved, 1L);
        saved.addBuddy(cloneWithId(new BuddyInfo("A", "1"), 100L));
        saved.addBuddy(cloneWithId(new BuddyInfo("B", "2"), 101L));
        saved.addBuddy(cloneWithId(new BuddyInfo("C", "3"), 102L));

        when(abRepo.save(any(AddressBook.class))).thenReturn(saved);

        mvc.perform(post("/addressbooks/1/buddies/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.buddies", hasSize(3)))
                .andExpect(jsonPath("$.buddies[*].name", containsInAnyOrder("A", "B", "C")));
    }

    // ---------- DELETE /addressbooks/{id}/buddies/{buddyId} ----------
    @Test
    void removeBuddy_returns204_andSaves() throws Exception {
        AddressBook ab = new AddressBook("Owner");
        setId(ab, 1L);
        BuddyInfo john = cloneWithId(new BuddyInfo("John", "123"), 10L);
        ab.addBuddy(john);

        when(abRepo.findById(1L)).thenReturn(Optional.of(ab));

        mvc.perform(delete("/addressbooks/1/buddies/10"))
                .andExpect(status().isNoContent());

        // ensure save called with buddy removed
        verify(abRepo).save(argThat(book ->
                book.getBuddies().stream().noneMatch(b -> Long.valueOf(10L).equals(b.getId()))
        ));
    }

    // ---------- DELETE /addressbooks/{id} ----------
    @Test
    void deleteBook_returns204() throws Exception {
        doNothing().when(abRepo).deleteById(1L);

        mvc.perform(delete("/addressbooks/1"))
                .andExpect(status().isNoContent());

        verify(abRepo).deleteById(1L);
    }

    // ===== helpers for id assignment in tests =====
    private static void setId(AddressBook ab, Long id) {
        try {
            var f = AddressBook.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(ab, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setId(BuddyInfo b, Long id) {
        try {
            var f = BuddyInfo.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(b, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static BuddyInfo cloneWithId(BuddyInfo src, Long id) {
        BuddyInfo copy = new BuddyInfo(src.getName(), src.getPhone());
        setId(copy, id);
        return copy;
    }
}
