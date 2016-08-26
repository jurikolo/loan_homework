package loan.controller;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import loan.Customer;
import loan.CustomerRepository;
import loan.Loan;
import loan.LoanRepository;
import loan.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by jurikolo on 26.08.16.
 */
@RestController
@RequestMapping("/loans")
class LoanRestController {
    private final static Logger log = LoggerFactory.getLogger(LoanRestController.class);

    private final LoanRepository loanRepository;
    private final CustomerRepository customerRepository;
    private static final AtomicLong cnt = new AtomicLong();

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

        String countryCode = Service.getCountryByIp(ip);
        log.info("Customer country code: " + countryCode);

        if (Service.validateRequest(body, customer, countryCode)) {
            log.info("Request is valid");
            //Add loan to DB
            loanRepository.save(new Loan(customer.get(), body.get("amount"), body.get("term"), true, countryCode));
            HttpHeaders httpHeaders = new HttpHeaders();
            return new ResponseEntity<Object>("success", httpHeaders, HttpStatus.CREATED);
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

    @Autowired
    LoanRestController(LoanRepository loanRepository, CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
        this.loanRepository = loanRepository;
    }
}
