package com.franchise.management.api.repository;

import com.franchise.management.api.entity.Franchise;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FranchiseRepository extends ReactiveCrudRepository<Franchise, Long> {
}