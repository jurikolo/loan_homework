package loan.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Created by jurikolo on 24.08.16.
 */
@Entity
public class Loan {

    @JsonIgnore
    @ManyToOne
    private Customer customer;

    @Id
    @GeneratedValue
    private Long id;

    public String amount;
    public String term;
    public Boolean valid;
    public String countryCode;

    Loan() {
        //JPA only
    }

    public Loan(Customer customer, String amount, String term, Boolean valid, String countryCode) {
        this.customer = customer;
        this.amount = amount;
        this.term = term;
        this.valid = valid;
        this.countryCode = countryCode;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Long getId() {
        return id;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getAmount() {
        return amount;
    }

    public String getTerm() {
        return term;
    }

    public Boolean getValid() {
        return valid;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
