package com.franchise.management.api.service;

import com.franchise.management.api.dto.*;
import com.franchise.management.api.entity.Branch;
import com.franchise.management.api.entity.Franchise;
import com.franchise.management.api.entity.Product;
import com.franchise.management.api.repository.BranchRepository;
import com.franchise.management.api.repository.FranchiseRepository;
import com.franchise.management.api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class FranchiseService {

    private final FranchiseRepository franchiseRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

    public Mono<Franchise> createFranchise(FranchiseRequest request) {
        Franchise franchise = new Franchise();
        franchise.setName(request.getName());
        return franchiseRepository.save(franchise);
    }

    public Mono<Branch> addBranchToFranchise(Long franchiseId, BranchRequest request) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new RuntimeException("Franchise not found with id: " + franchiseId)))
                .flatMap(franchise -> {
                    Branch branch = new Branch();
                    branch.setName(request.getName());
                    branch.setFranchiseId(franchiseId);
                    return branchRepository.save(branch);
                });
    }

    public Mono<Product> addProductToBranch(Long branchId, ProductRequest request) {
        return branchRepository.findById(branchId)
                .switchIfEmpty(Mono.error(new RuntimeException("Branch not found with id: " + branchId)))
                .flatMap(branch -> {
                    Product product = new Product();
                    product.setName(request.getName());
                    product.setStock(request.getStock());
                    product.setBranchId(branchId);
                    return productRepository.save(product);
                });
    }

    public Mono<Void> deleteProduct(Long productId) {
        return productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new RuntimeException("Product not found with id: " + productId)))
                .flatMap(product -> productRepository.delete(product));
    }

    public Mono<Product> updateProductStock(Long productId, UpdateStockRequest request) {
        return productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new RuntimeException("Product not found with id: " + productId)))
                .flatMap(product -> {
                    product.setStock(request.getStock());
                    return productRepository.save(product);
                });
    }

    public Flux<ProductResponse> getTopStockProductsByFranchise(Long franchiseId) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new RuntimeException("Franchise not found with id: " + franchiseId)))
                .flatMapMany(franchise ->
                    productRepository.findTopStockProductsByFranchise(franchiseId)
                        .flatMap(product ->
                            branchRepository.findById(product.getBranchId())
                                .map(branch -> new ProductResponse(
                                    product.getId(),
                                    product.getName(),
                                    product.getStock(),
                                    branch.getId(),
                                    branch.getName()
                                ))
                        )
                );
    }

    public Mono<Franchise> updateFranchiseName(Long franchiseId, UpdateNameRequest request) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new RuntimeException("Franchise not found with id: " + franchiseId)))
                .flatMap(franchise -> {
                    franchise.setName(request.getName());
                    return franchiseRepository.save(franchise);
                });
    }

    public Mono<Branch> updateBranchName(Long branchId, UpdateNameRequest request) {
        return branchRepository.findById(branchId)
                .switchIfEmpty(Mono.error(new RuntimeException("Branch not found with id: " + branchId)))
                .flatMap(branch -> {
                    branch.setName(request.getName());
                    return branchRepository.save(branch);
                });
    }

    public Mono<Product> updateProductName(Long productId, UpdateNameRequest request) {
        return productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new RuntimeException("Product not found with id: " + productId)))
                .flatMap(product -> {
                    product.setName(request.getName());
                    return productRepository.save(product);
                });
    }

    public Flux<Franchise> getAllFranchises() {
        return franchiseRepository.findAll();
    }

    public Mono<FranchiseResponse> getFranchiseById(Long franchiseId) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(new RuntimeException("Franchise not found with id: " + franchiseId)))
                .flatMap(franchise ->
                    branchRepository.findByFranchiseId(franchiseId)
                        .flatMap(branch ->
                            productRepository.findByBranchId(branch.getId())
                                .map(product -> new ProductSimpleResponse(
                                    product.getId(),
                                    product.getName(),
                                    product.getStock()
                                ))
                                .collectList()
                                .map(products -> new BranchResponse(
                                    branch.getId(),
                                    branch.getName(),
                                    branch.getFranchiseId(),
                                    products
                                ))
                        )
                        .collectList()
                        .map(branches -> new FranchiseResponse(
                            franchise.getId(),
                            franchise.getName(),
                            branches
                        ))
                );
    }
}