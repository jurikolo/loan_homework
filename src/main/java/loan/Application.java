package loan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
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
        Customer customer = customerRepository.save(new Customer("Ivan", "Susanin", "12345"));
        loanRepository.save(new Loan(customer, "100", "200", true));
        loanRepository.save(new Loan(customer, "120", "220", true));
        loanRepository.save(new Loan(customer, "120", "220", false));

        customer = customerRepository.save(new Customer("Susan", "Ivanin", "12346"));
        loanRepository.save(new Loan(customer, "100500", "200300", false));
        loanRepository.save(new Loan(customer, "6", "8", false));
        loanRepository.save(new Loan(customer, "300200", "500600", false));

        customer = customerRepository.save(new Customer("Tamar", "Ramak", "12347"));

        customer = customerRepository.save(new Customer("Valid", "User", "12348"));
        loanRepository.save(new Loan(customer, "100500", "200300", true));
        return null;
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
    ResponseEntity<?> handle() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(URI.create("http://whateverhost:8080/loans"));
        return new ResponseEntity<Object>(this.findAllValid(), httpHeaders, HttpStatus.OK);
    }

    @RequestMapping(value = "/privateId/{privateId}", method = RequestMethod.GET)
    ResponseEntity<?> readLoansByPrivateId(@PathVariable String privateId) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(URI.create("http://whateverhost:8080/loans"));
        return new ResponseEntity<Object>(this.findAllValidByPrivateId(privateId), httpHeaders, HttpStatus.OK);
    }

    Collection<Loan> findAllValid() {
        Collection<Loan> loans = loanRepository.findAll();
        Collection<Loan> validLoans = new ArrayList<>();
        for(Loan loan : loans) {
            if (loan.getValid()) {
                validLoans.add(loan);
            }
        }
        return validLoans;
    }

    Collection<Loan> findAllValidByPrivateId(String personalId) {
        Collection<Loan> loans = loanRepository.findByCustomerPersonalId(personalId);
        Collection<Loan> validLoans = new ArrayList<>();
        for(Loan loan : loans) {
            if (loan.getValid()) {
                validLoans.add(loan);
            }
        }
        return validLoans;
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

    @RequestMapping(method = RequestMethod.GET)
    ResponseEntity<?> handle() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(URI.create("http://localhost:8080/customer"));
        return new ResponseEntity<Object>(customerRepository.findAll(), httpHeaders, HttpStatus.OK);
    }
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class PersonalIdNotFoundException extends RuntimeException {

    public PersonalIdNotFoundException(String personalId) {
        super("could not find user '" + personalId + "'.");
    }
}