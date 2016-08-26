package loan.dao;

import loan.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Created by jurikolo on 24.08.16.
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByPersonalId(String personalId);
}
