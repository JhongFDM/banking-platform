package com.group1.banking.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.group1.banking.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByAccount_Customer_CustomerIdAndTimestampBetweenOrderByTimestampAsc(
            Long customerId, Instant start, Instant end);
}
