package loan;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Created by jurikolo on 24.08.16.
 */
@Entity
public class Loan {

    @ManyToOne
    private Customer customer;

    @Id
    @GeneratedValue
    private Long id;

    public String amount;
    public String term;

    Loan() {
        //JPA only
    }

    public Loan(Customer customer, String amount, String term) {
        this.customer = customer;
        this.amount = amount;
        this.term = term;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }
}
