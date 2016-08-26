package service;

import loan.model.Customer;
import org.junit.Test;

import loan.service.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Created by jurikolo on 26.08.16.
 */
public class ServiceTests {
    @Test
    public void EmptyBodyValidationRequest() {
        Map<String, String> request = Collections.emptyMap();
        Customer customer = new Customer("name", "surname", "12345", false);
        assertFalse(Service.validateRequest(request, Optional.of(customer), "LV"));
    }

    @Test
    public void EmptyCountryCodeValidationRequest() {
        Map<String, String> request = new HashMap<>();
        request.put("amount", "100");
        request.put("term", "20");
        request.put("personalId", "12345");
        request.put("name", "name");
        request.put("surname", "surname");
        Customer customer = new Customer("name", "surname", "12345", false);
        assertTrue(Service.validateRequest(request, Optional.of(customer), ""));
    }

    @Test
    public void BlackListedValidationRequest() {
        Map<String, String> request = new HashMap<>();
        request.put("amount", "100");
        request.put("term", "20");
        request.put("personalId", "12345");
        request.put("name", "name");
        request.put("surname", "surname");
        Customer customer = new Customer("name", "surname", "12345", true);
        assertFalse(Service.validateRequest(request, Optional.of(customer), "LV"));
    }

    @Test
    public void EmptyIp() {
        assertNotNull(Service.getCountryByIp(""));
    }

    @Test
    public void UsIp() {
        assertEquals("US", Service.getCountryByIp("1.2.3.4"));
    }
}
