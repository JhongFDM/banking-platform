package com.group1.banking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.group1.banking.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
	
	Optional<Customer> findByCustomerIdAndDeletedAtIsNull(Long customerId);

	List<Customer> findAllByDeletedAtIsNullOrderByCustomerIdAsc();

    boolean existsByCustomerIdAndDeletedAtIsNull(Long customerId);
}
