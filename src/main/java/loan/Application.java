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
import org.springframework.web.client.RestTemplate;

import java.net.SocketException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by jurikolo on 25.08.16.
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {
    private final static Logger log = LoggerFactory.getLogger(Application.class);
    @Bean
    //Generate initial data
    CommandLineRunner init(CustomerRepository customerRepository, LoanRepository loanRepository) {
        Customer customer = customerRepository.save(new Customer("Ivan", "Susanin", "12345", false));
        loanRepository.save(new Loan(customer, "100", "200", true, "LV"));
        loanRepository.save(new Loan(customer, "120", "220", true, "LV"));
        loanRepository.save(new Loan(customer, "120", "220", false, "SE"));

        customer = customerRepository.save(new Customer("Susan", "Ivanin", "12346", false));
        loanRepository.save(new Loan(customer, "100500", "200300", false, "NO"));
        loanRepository.save(new Loan(customer, "6", "8", false, "ES"));
        loanRepository.save(new Loan(customer, "300200", "500600", false, "LV"));

        customer = customerRepository.save(new Customer("Tamar", "Ramak", "12347", false));

        customer = customerRepository.save(new Customer("Valid", "User", "12348", true));
        loanRepository.save(new Loan(customer, "100500", "200300", true, "LX"));

        log.info("Initial customers and loans added to DB");

        return null;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
@RequestMapping("/loans")
class LoanRestController {
    private final static Logger log = LoggerFactory.getLogger(LoanRestController.class);

    private final LoanRepository loanRepository;
    private final CustomerRepository customerRepository;

    //return all the valid loans
    @RequestMapping(method = RequestMethod.GET)
    ResponseEntity<?> readValidLoans() {
        log.info("Received /loans GET request");
        HttpHeaders httpHeaders = new HttpHeaders();
        return new ResponseEntity<Object>(this.findAllValid(), httpHeaders, HttpStatus.OK);
    }

    //return all the valid loans by personalId
    @RequestMapping(value = "/personalId/{personalId}", method = RequestMethod.GET)
    ResponseEntity<?> readValidLoansBypersonalId(@PathVariable String personalId) {
        log.info(String.format("Received /loans/personalId/%s GET request", personalId));
        HttpHeaders httpHeaders = new HttpHeaders();
        return new ResponseEntity<Object>(this.findAllValidBypersonalId(personalId), httpHeaders, HttpStatus.OK);
    }

    //add new loan
    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<?> postLoan(@RequestBody Map<String,String> body, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        log.info("Received /loans POST request from " + ip);
        Optional<Customer> customer = customerRepository.findByPersonalId(body.get("personalId"));

        String countryCode = getCountryByIp(ip);
        log.info("Customer country code: " + countryCode);

        if (validateRequest(body, customer)) {
            log.info("Request is valid");
            //Add loan to DB
            loanRepository.save(new Loan(customer.get(), body.get("amount"), body.get("term"), true, countryCode));
            HttpHeaders httpHeaders = new HttpHeaders();
            return new ResponseEntity<Object>("success", httpHeaders, HttpStatus.OK);
        }
        else {
            log.info("Request is invalid");
            HttpHeaders httpHeaders = new HttpHeaders();
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
        log.info("Verify amount parameter is present in a request");
        if (null == request.get("amount")) return false;
        log.info("Verify term parameter is present in a request");
        if (null == request.get("term")) return false;
        log.info("Verify name parameter is present in a request");
        if (null == request.get("name")) return false;
        log.info("Verify surname parameter is present in a request");
        if (null == request.get("surname")) return false;
        log.info("Verify personalId parameter is present in a request");
        if (null == request.get("personalId")) return false;

        //verify blacklist
        log.info("Verify whether customer is blacklisted");
        if (!customer.isPresent() || customer.get().getBlackListed()) return false;

        //get country

        //verify tps

        return true;
    }

    private String getCountryByIp(String ip) {
        final String uri = "http://ip-api.com/json/" + ip;
        Map<String, String> result = Collections.emptyMap();
        RestTemplate restTemplate = new RestTemplate();
        try {
            result = restTemplate.getForObject(uri, Map.class);
        } catch (Exception e) {
            log.error("Unable to resolve country code by ip: " + e.getMessage(), e);
            return "LV";
        }
        if (result.containsKey("countryCode")) return result.get("countryCode");
        else return "LV";
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