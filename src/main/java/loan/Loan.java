package loan;

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

    Loan() {
        //JPA only
    }

    public Loan(Customer customer, String amount, String term, Boolean valid) {
        this.customer = customer;
        this.amount = amount;
        this.term = term;
        this.valid = valid;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Long getId() {
        return id;
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

}
