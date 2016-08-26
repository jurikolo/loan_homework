package controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.hamcrest.Matchers.is;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jurikolo on 26.08.16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootConfiguration()
@WebAppConfiguration
@ContextConfiguration(classes = Application.class)
public class CustomerRestControllerTests {
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

        this.customer = customerRepository.save(new Customer("Ivan", "Susanin", "12345", false));
        this.loanList.add(loanRepository.save(new Loan(customer, "100", "200", true, "LV")));
        this.loanList.add(loanRepository.save(new Loan(customer, "120", "220", true, "LV")));
        this.loanList.add(loanRepository.save(new Loan(customer, "120", "220", false, "SE")));

        customer = customerRepository.save(new Customer("Valid", "User", "12348", true));
        loanRepository.save(new Loan(customer, "100500", "200300", true, "LX"));
    }

    @Test
    public void noParamRequestShouldFail() throws Exception {
        this.mockMvc.perform(get("/customer/")).andDo(print()).andExpect(status().is4xxClientError());
    }

    @Test
    public void NonExistingPersonalIdRequestShouldFail() throws Exception {
        this.mockMvc.perform(get("/customer/" + this.unknownPersonalId))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Customer not found"));

    }

    @Test
    public void BlackListedPersonalIdRequestShouldSucceed() throws Exception {
        this.mockMvc.perform(get("/customer/" + this.blackListedPersonalId))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.blackListed", is(true)));

    }

    @Test
    public void WhiteListedPersonalIdRequestShouldSucceed() throws Exception {
        this.mockMvc.perform(get("/customer/" + this.whiteListedPersonalId))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.blackListed", is(false)));

    }
}
