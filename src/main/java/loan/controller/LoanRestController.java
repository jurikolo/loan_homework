package loan.controller;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import loan.Customer;
import loan.CustomerRepository;
import loan.Loan;
import loan.LoanRepository;
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
    private static final int cntLimit = 2;
    private static final long timeLimit = 10;
    private static final Cache<String, Integer> limitMap = CacheBuilder.newBuilder()
            .expireAfterWrite(timeLimit, TimeUnit.SECONDS)
            .build();

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

        if (validateRequest(body, customer, countryCode)) {
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

    Boolean validateRequest(Map<String, String> request, Optional<Customer> customer, String countryCode) {
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

        //verify tps
        if (limitMap.asMap().containsKey(countryCode)) {
            if (limitMap.asMap().get(countryCode) > cntLimit) {
                HttpHeaders httpHeaders = new HttpHeaders();
                return false;
            } else {
                limitMap.asMap().put(countryCode, limitMap.asMap().get(countryCode) + 1);
            }
        } else {
            limitMap.asMap().put(countryCode, 1);
        }
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
