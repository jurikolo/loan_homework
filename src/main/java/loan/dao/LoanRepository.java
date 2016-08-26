package loan.dao;

import loan.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;


/**
 * Created by jurikolo on 24.08.16.
 */
public interface LoanRepository extends JpaRepository<Loan, Long> {
    Collection<Loan> findByCustomerPersonalId(String personalId);
}
