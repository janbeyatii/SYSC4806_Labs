    package lab1;

    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;

    // http://localhost:8080/h2-console

    //JDBC URL: jdbc:h2:mem:addressbook
    //User Name: sa
    //Password: (leave blank)

    //GUI
    // https://4806addressbook-ahe8ffesd8c3haey.northcentralus-01.azurewebsites.net/addressbooks/1/view
    @SpringBootApplication
    public class AddressBookApp {
        public static void main(String[] args) {
            SpringApplication.run(AddressBookApp.class, args);
        }
    }
