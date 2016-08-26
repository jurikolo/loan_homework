package controller;

import loan.*;
import loan.dao.CustomerRepository;
import loan.dao.LoanRepository;
import loan.model.Customer;
import loan.model.Loan;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.Charset;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Created by jurikolo on 26.08.16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootConfiguration()
@WebAppConfiguration
@ContextConfiguration(classes = Application.class)
public class LoanRestControllerTests {
    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));
    private MockMvc mockMvc;
    private String unknownPersonalId = "123";
    private String blackListedPersonalId = "12348";
    private String whiteListedPersonalId = "12345";

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    private Customer customer;
    private List<Loan> loanList = new ArrayList<>();

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream().filter(
                hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().get();

        Assert.assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        this.loanRepository.deleteAllInBatch();
        this.customerRepository.deleteAllInBatch();

        customerRepository.save(new Customer("Ivan", "Susanin", "12345", false));
        this.loanList.add(loanRepository.save(new Loan(customer, "100", "200", true, "LV")));
        this.loanList.add(loanRepository.save(new Loan(customer, "120", "220", true, "LV")));
        this.loanList.add(loanRepository.save(new Loan(customer, "120", "220", false, "SE")));

        customerRepository.save(new Customer("Valid", "User", "12348", true));
        loanRepository.save(new Loan(customer, "100500", "200300", true, "LX"));
    }

    @Test
    public void noParamRequestShouldFail() throws Exception {
        this.mockMvc.perform(get("/loans/")).andDo(print())
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void WhiteListedCustomerRequestShouldSucceed() throws Exception {
        this.mockMvc.perform(get("/loans/personalId/" + whiteListedPersonalId)).andDo(print())
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void BlackListedCustomerRequestShouldSucceed() throws Exception {
        this.mockMvc.perform(get("/loans/personalId/" + blackListedPersonalId)).andDo(print())
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void UnknownCustomerRequestShouldSucceed() throws Exception {
        this.mockMvc.perform(get("/loans/personalId/" + unknownPersonalId)).andDo(print())
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void noParamCustomerRequestShouldFail() throws Exception {
        this.mockMvc.perform(get("/loans/personalId")).andDo(print()).andExpect(status().is4xxClientError());
    }

    @Test
    public void requestLoanByUnknownCustomer() throws Exception {
        this.mockMvc.perform(post("/loans").contentType(contentType).content(String.format("{ \"amount\" : \"600\", \"term\" : \"24\", \"personalId\" : \"%s\", \"name\" : \"Ivan\", \"surname\" : \"Susanin\" }", unknownPersonalId))).andExpect(status().isBadRequest());
    }

    @Test
    public void requestLoanByBlackListedCustomer() throws Exception {
        this.mockMvc.perform(post("/loans").contentType(contentType).content(String.format("{ \"amount\" : \"600\", \"term\" : \"24\", \"personalId\" : \"%s\", \"name\" : \"Ivan\", \"surname\" : \"Susanin\" }", blackListedPersonalId))).andExpect(status().isBadRequest());
    }

    @Test
    public void requestLoanByWhiteListedCustomer() throws Exception {
        this.mockMvc.perform(post("/loans").contentType(contentType).content(String.format("{ \"amount\" : \"600\", \"term\" : \"24\", \"personalId\" : \"%s\", \"name\" : \"Ivan\", \"surname\" : \"Susanin\" }", whiteListedPersonalId))).andExpect(status().isCreated());
    }
}
