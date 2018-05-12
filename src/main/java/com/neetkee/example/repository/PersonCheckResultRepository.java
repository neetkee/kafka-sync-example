package com.neetkee.example.repository;

import com.neetkee.example.model.PersonCheckResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonCheckResultRepository extends JpaRepository<PersonCheckResult, Integer> {
    Optional<PersonCheckResult> findByPersonId(Integer personId);
}
