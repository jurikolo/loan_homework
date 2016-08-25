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
import java.util.*;

/**
 * Created by jurikolo on 25.08.16.
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {
    @Bean
    CommandLineRunner init(CustomerRepository customerRepository, LoanRepository loanRepository) {
        Customer customer = customerRepository.save(new Customer("Ivan", "Susanin", "12345", false));
        loanRepository.save(new Loan(customer, "100", "200", true));
        loanRepository.save(new Loan(customer, "120", "220", true));
        loanRepository.save(new Loan(customer, "120", "220", false));

        customer = customerRepository.save(new Customer("Susan", "Ivanin", "12346", false));
        loanRepository.save(new Loan(customer, "100500", "200300", false));
        loanRepository.save(new Loan(customer, "6", "8", false));
        loanRepository.save(new Loan(customer, "300200", "500600", false));

        customer = customerRepository.save(new Customer("Tamar", "Ramak", "12347", false));

        customer = customerRepository.save(new Customer("Valid", "User", "12348", true));
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
        return new ResponseEntity<Object>(this.findAllValid(), httpHeaders, HttpStatus.OK);
    }

    @RequestMapping(value = "/personalId/{personalId}", method = RequestMethod.GET)
    ResponseEntity<?> readLoansBypersonalId(@PathVariable String personalId) {
        HttpHeaders httpHeaders = new HttpHeaders();
        return new ResponseEntity<Object>(this.findAllValidBypersonalId(personalId), httpHeaders, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<?> postLoan(@RequestBody Map<String,String> body) {
        Optional<Customer> customer = customerRepository.findByPersonalId(body.get("personalId"));

        HttpHeaders httpHeaders = new HttpHeaders();
        if (validateRequest(body, customer)) {
            System.out.println("Request is valid");
            //Add loan to DB
            Loan loan = new Loan();
            loan.setAmount(body.get("amount"));
            loan.setTerm(body.get("term"));
            loanRepository.save(new Loan(customer.get(), body.get("amount"), body.get("term"), true));
            return new ResponseEntity<Object>("success", httpHeaders, HttpStatus.OK);
        }
        else {
            System.out.println("Request is invalid");
            return new ResponseEntity<Object>("failure", httpHeaders, HttpStatus.BAD_REQUEST);
        }

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

    Collection<Loan> findAllValidBypersonalId(String personalId) {
        Collection<Loan> loans = loanRepository.findByCustomerPersonalId(personalId);
        Collection<Loan> validLoans = new ArrayList<>();
        for(Loan loan : loans) {
            if (loan.getValid()) {
                validLoans.add(loan);
            }
        }
        return validLoans;
    }

    Boolean validateRequest(Map<String, String> request, Optional<Customer> customer) {
        //verify entered parameters exists
        System.out.println("Check amount");
        if (null == request.get("amount")) return false;
        System.out.println("Check term");
        if (null == request.get("term")) return false;
        System.out.println("Check name");
        if (null == request.get("name")) return false;
        System.out.println("Check surname");
        if (null == request.get("surname")) return false;
        System.out.println("Check personalId");
        if (null == request.get("personalId")) return false;

        //verify blacklist
        System.out.println("Check blacklist");
        if (!customer.isPresent() || customer.get().getBlackListed()) return false;

        //verify tps

        return true;
    }

    @Autowired
    LoanRestController(LoanRepository loanRepository, CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
        this.loanRepository = loanRepository;
    }
}

@RestController
@RequestMapping("/customer/{personalId}")
class CustomerRestController {
    private final CustomerRepository customerRepository;

    @Autowired
    CustomerRestController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @RequestMapping(method = RequestMethod.GET)
    ResponseEntity<?> getCustomer(@PathVariable String personalId) {
        HttpHeaders httpHeaders = new HttpHeaders();
        //httpHeaders.setLocation(URI.create("http://localhost:8080/customer/{personalId}"));
        return new ResponseEntity<Object>(customerRepository.findByPersonalId(personalId).get(), httpHeaders, HttpStatus.OK);
    }
}