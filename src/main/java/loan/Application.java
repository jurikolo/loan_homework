package loan;

import loan.dao.CustomerRepository;
import loan.dao.LoanRepository;
import loan.model.Customer;
import loan.model.Loan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

