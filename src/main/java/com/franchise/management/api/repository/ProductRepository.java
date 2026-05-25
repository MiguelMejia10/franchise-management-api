package com.franchise.management.api.repository;

import com.franchise.management.api.entity.Product;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ProductRepository extends ReactiveCrudRepository<Product, Long> {

    Flux<Product> findByBranchId(Long branchId);

    @Query("""
        SELECT p.* FROM products p
        INNER JOIN branches b ON p.branch_id = b.id
        WHERE b.franchise_id = :franchiseId
        AND p.stock = (
            SELECT MAX(p2.stock)
            FROM products p2
            WHERE p2.branch_id = p.branch_id
        )
        ORDER BY b.name, p.name
    """)
    Flux<Product> findTopStockProductsByFranchise(Long franchiseId);
}