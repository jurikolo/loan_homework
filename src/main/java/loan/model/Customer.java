package loan.model;

import loan.model.Loan;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jurikolo on 24.08.16.
 */

@Entity
public class Customer {

    @OneToMany(mappedBy = "customer")
    private Set<Loan> loans = new HashSet<>();

    @Id
    @GeneratedValue
    private Long id;

    public String name;
    public String surname;
    public String personalId;
    public Boolean blackListed;

    public Set<Loan> getLoans() {
        return loans;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPersonalId() {
        return personalId;
    }

    public void setPersonalId(String personalId) {
        this.personalId = personalId;
    }

    public Boolean getBlackListed() {
        return blackListed;
    }

    public Customer(String name, String surname, String personalId, Boolean blackListed) {
        this.name = name;
        this.surname = surname;
        this.personalId = personalId;
        this.blackListed = blackListed;
    }

    Customer() {
        //JPA only
    }
}
