package loan.controller;

import loan.Customer;
import loan.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

/**
 * Created by jurikolo on 26.08.16.
 */
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
        Customer customer;
        try {
            customer = customerRepository.findByPersonalId(personalId).get();
        } catch (NoSuchElementException e) {
            return new ResponseEntity<String>("Customer not found", httpHeaders, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Object>(customer, httpHeaders, HttpStatus.OK);
    }
}
