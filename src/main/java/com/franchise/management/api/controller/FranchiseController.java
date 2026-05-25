package com.franchise.management.api.controller;

import com.franchise.management.api.dto.*;
import com.franchise.management.api.entity.Branch;
import com.franchise.management.api.entity.Franchise;
import com.franchise.management.api.entity.Product;
import com.franchise.management.api.service.FranchiseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/franchises")
@RequiredArgsConstructor
@Tag(name = "Franchise Management", description = "Reactive API for managing franchises, branches and products")
public class FranchiseController {

    private final FranchiseService franchiseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new franchise")
    public Mono<Franchise> createFranchise(@Valid @RequestBody FranchiseRequest request) {
        return franchiseService.createFranchise(request);
    }

    @GetMapping
    @Operation(summary = "Get all franchises")
    public Flux<Franchise> getAllFranchises() {
        return franchiseService.getAllFranchises();
    }

    @GetMapping("/{franchiseId}")
    @Operation(summary = "Get franchise by ID with branches and products")
    public Mono<FranchiseResponse> getFranchiseById(@PathVariable Long franchiseId) {
        return franchiseService.getFranchiseById(franchiseId);
    }

    @PatchMapping("/{franchiseId}/name")
    @Operation(summary = "Update franchise name")
    public Mono<Franchise> updateFranchiseName(
            @PathVariable Long franchiseId,
            @Valid @RequestBody UpdateNameRequest request) {
        return franchiseService.updateFranchiseName(franchiseId, request);
    }

    @PostMapping("/{franchiseId}/branches")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a new branch to a franchise")
    public Mono<Branch> addBranchToFranchise(
            @PathVariable Long franchiseId,
            @Valid @RequestBody BranchRequest request) {
        return franchiseService.addBranchToFranchise(franchiseId, request);
    }

    @PatchMapping("/branches/{branchId}/name")
    @Operation(summary = "Update branch name")
    public Mono<Branch> updateBranchName(
            @PathVariable Long branchId,
            @Valid @RequestBody UpdateNameRequest request) {
        return franchiseService.updateBranchName(branchId, request);
    }

    @PostMapping("/branches/{branchId}/products")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a new product to a branch")
    public Mono<Product> addProductToBranch(
            @PathVariable Long branchId,
            @Valid @RequestBody ProductRequest request) {
        return franchiseService.addProductToBranch(branchId, request);
    }

    @DeleteMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a product")
    public Mono<Void> deleteProduct(@PathVariable Long productId) {
        return franchiseService.deleteProduct(productId);
    }

    @PatchMapping("/products/{productId}/stock")
    @Operation(summary = "Update product stock")
    public Mono<Product> updateProductStock(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateStockRequest request) {
        return franchiseService.updateProductStock(productId, request);
    }

    @PatchMapping("/products/{productId}/name")
    @Operation(summary = "Update product name")
    public Mono<Product> updateProductName(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateNameRequest request) {
        return franchiseService.updateProductName(productId, request);
    }

    @GetMapping("/{franchiseId}/top-stock-products")
    @Operation(summary = "Get products with highest stock per branch for a franchise",
               description = "Returns a reactive stream of products that have the maximum stock in each branch of the specified franchise")
    public Flux<ProductResponse> getTopStockProductsByFranchise(@PathVariable Long franchiseId) {
        return franchiseService.getTopStockProductsByFranchise(franchiseId);
    }
}