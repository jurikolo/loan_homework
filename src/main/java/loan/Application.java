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
        loanRepository.save(new Loan(customer, "100", "200"));
        loanRepository.save(new Loan(customer, "120", "220"));

        customer = customerRepository.save(new Customer("Susan", "Ivanin", "12346"));
        loanRepository.save(new Loan(customer, "100500", "200300"));
        loanRepository.save(new Loan(customer, "300200", "500600"));

        customer = customerRepository.save(new Customer("Tamar", "Ramak", "12347"));

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
//    Collection<Loan> readLoans() {
//        return this.loanRepository.findAll();
//    }
    ResponseEntity<?> handle() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(URI.create("http://localhost:8080/loans"));
        return new ResponseEntity<Object>(loanRepository.findAll(), httpHeaders, HttpStatus.OK);
    }

    @RequestMapping(value = "/privateId/{privateId}", method = RequestMethod.GET)
    Collection<Loan> readLoansByPrivateId(@PathVariable String privateId) {
        return loanRepository.findByCustomerPersonalId(privateId);
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
