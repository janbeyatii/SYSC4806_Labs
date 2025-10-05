package lab1;

import org.springframework.data.repository.CrudRepository;

public interface BuddyInfoRepository extends CrudRepository<BuddyInfo, Long> {
    Iterable<BuddyInfo> findByName(String name);
    Iterable<BuddyInfo> findByNameAndAddress(String name, String address); // add this

}
