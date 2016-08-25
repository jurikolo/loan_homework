package loan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by jurikolo on 25.08.16.
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {
    @Bean
    CommandLineRunner init(CustomerRepository customerRepository, LoanRepository loanRepository) {
        return (evt) -> Arrays.asList("12345,23456,34567,45678,56789".split(","))
                .forEach(
                        a -> {
                            Customer customer = customerRepository.save(new Customer("Ivan", "Susanin", a));
                            loanRepository.save(new Loan(customer, "100", "200"));
                        }
                );
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
@RequestMapping("/loans")
class LoanRestController {
    private final LoanRepository loanRepository;
    private final CustomerRepository customerRepository;

    @RequestMapping(method = RequestMethod.GET)
    Collection<Loan> readLoans() {
        return this.loanRepository.findAll();
    }

    @Autowired
    LoanRestController(LoanRepository loanRepository, CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
        this.loanRepository = loanRepository;
    }

}

@RestController
@RequestMapping("/customer")
class CustomerRestController {
    private final CustomerRepository customerRepository;

    @Autowired
    CustomerRestController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @RequestMapping
    ResponseEntity<?> handle() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(URI.create("myCustomLocation"));
        return new ResponseEntity<Object>(customerRepository.findByPersonalId("123456"), httpHeaders, HttpStatus.OK);
    }

}

@ResponseStatus(HttpStatus.NOT_FOUND)
class PersonalIdNotFoundException extends RuntimeException {

    public PersonalIdNotFoundException(String personalId) {
        super("could not find user '" + personalId + "'.");
    }
}
