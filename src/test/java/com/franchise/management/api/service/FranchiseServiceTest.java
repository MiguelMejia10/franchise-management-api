package com.franchise.management.api.service;

import com.franchise.management.api.dto.*;
import com.franchise.management.api.entity.Branch;
import com.franchise.management.api.entity.Franchise;
import com.franchise.management.api.entity.Product;
import com.franchise.management.api.repository.BranchRepository;
import com.franchise.management.api.repository.FranchiseRepository;
import com.franchise.management.api.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FranchiseServiceTest {

    @Mock
    private FranchiseRepository franchiseRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private FranchiseService franchiseService;

    @Test
    void createFranchise_shouldSaveAndReturnFranchise() {
        // Given
        FranchiseRequest request = new FranchiseRequest("Test Franchise");
        Franchise savedFranchise = new Franchise(1L, "Test Franchise");

        when(franchiseRepository.save(any(Franchise.class)))
                .thenReturn(Mono.just(savedFranchise));

        // When
        Mono<Franchise> result = franchiseService.createFranchise(request);

        // Then
        StepVerifier.create(result)
                .assertNext(franchise -> {
                    assertThat(franchise.getId()).isEqualTo(1L);
                    assertThat(franchise.getName()).isEqualTo("Test Franchise");
                })
                .verifyComplete();

        verify(franchiseRepository, times(1)).save(any(Franchise.class));
    }

    @Test
    void addBranchToFranchise_whenFranchiseExists_shouldSaveBranch() {
        // Given
        Long franchiseId = 1L;
        BranchRequest request = new BranchRequest("Test Branch");
        Franchise franchise = new Franchise(franchiseId, "Test Franchise");
        Branch savedBranch = new Branch(1L, "Test Branch", franchiseId);

        when(franchiseRepository.findById(franchiseId))
                .thenReturn(Mono.just(franchise));
        when(branchRepository.save(any(Branch.class)))
                .thenReturn(Mono.just(savedBranch));

        // When
        Mono<Branch> result = franchiseService.addBranchToFranchise(franchiseId, request);

        // Then
        StepVerifier.create(result)
                .assertNext(branch -> {
                    assertThat(branch.getId()).isEqualTo(1L);
                    assertThat(branch.getName()).isEqualTo("Test Branch");
                    assertThat(branch.getFranchiseId()).isEqualTo(franchiseId);
                })
                .verifyComplete();

        verify(franchiseRepository, times(1)).findById(franchiseId);
        verify(branchRepository, times(1)).save(any(Branch.class));
    }

    @Test
    void addBranchToFranchise_whenFranchiseNotFound_shouldReturnError() {
        // Given
        Long franchiseId = 999L;
        BranchRequest request = new BranchRequest("Test Branch");

        when(franchiseRepository.findById(franchiseId))
                .thenReturn(Mono.empty());

        // When
        Mono<Branch> result = franchiseService.addBranchToFranchise(franchiseId, request);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                        throwable.getMessage().contains("Franchise not found"))
                .verify();

        verify(franchiseRepository, times(1)).findById(franchiseId);
        verify(branchRepository, never()).save(any(Branch.class));
    }

    @Test
    void addProductToBranch_whenBranchExists_shouldSaveProduct() {
        // Given
        Long branchId = 1L;
        ProductRequest request = new ProductRequest("Laptop", 50);
        Branch branch = new Branch(branchId, "Test Branch", 1L);
        Product savedProduct = new Product(1L, "Laptop", 50, branchId);

        when(branchRepository.findById(branchId))
                .thenReturn(Mono.just(branch));
        when(productRepository.save(any(Product.class)))
                .thenReturn(Mono.just(savedProduct));

        // When
        Mono<Product> result = franchiseService.addProductToBranch(branchId, request);

        // Then
        StepVerifier.create(result)
                .assertNext(product -> {
                    assertThat(product.getId()).isEqualTo(1L);
                    assertThat(product.getName()).isEqualTo("Laptop");
                    assertThat(product.getStock()).isEqualTo(50);
                    assertThat(product.getBranchId()).isEqualTo(branchId);
                })
                .verifyComplete();

        verify(branchRepository, times(1)).findById(branchId);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void addProductToBranch_whenBranchNotFound_shouldReturnError() {
        // Given
        Long branchId = 999L;
        ProductRequest request = new ProductRequest("Laptop", 50);

        when(branchRepository.findById(branchId))
                .thenReturn(Mono.empty());

        // When
        Mono<Product> result = franchiseService.addProductToBranch(branchId, request);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                        throwable.getMessage().contains("Branch not found"))
                .verify();
    }

    @Test
    void deleteProduct_whenProductExists_shouldDeleteSuccessfully() {
        // Given
        Long productId = 1L;
        Product product = new Product(productId, "Laptop", 50, 1L);

        when(productRepository.findById(productId))
                .thenReturn(Mono.just(product));
        when(productRepository.delete(product))
                .thenReturn(Mono.empty());

        // When
        Mono<Void> result = franchiseService.deleteProduct(productId);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).delete(product);
    }

    @Test
    void deleteProduct_whenProductNotFound_shouldReturnError() {
        // Given
        Long productId = 999L;

        when(productRepository.findById(productId))
                .thenReturn(Mono.empty());

        // When
        Mono<Void> result = franchiseService.deleteProduct(productId);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                        throwable.getMessage().contains("Product not found"))
                .verify();

        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void updateProductStock_shouldUpdateAndReturnProduct() {
        // Given
        Long productId = 1L;
        UpdateStockRequest request = new UpdateStockRequest(100);
        Product product = new Product(productId, "Laptop", 50, 1L);
        Product updatedProduct = new Product(productId, "Laptop", 100, 1L);

        when(productRepository.findById(productId))
                .thenReturn(Mono.just(product));
        when(productRepository.save(any(Product.class)))
                .thenReturn(Mono.just(updatedProduct));

        // When
        Mono<Product> result = franchiseService.updateProductStock(productId, request);

        // Then
        StepVerifier.create(result)
                .assertNext(p -> {
                    assertThat(p.getStock()).isEqualTo(100);
                })
                .verifyComplete();

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateFranchiseName_shouldUpdateAndReturnFranchise() {
        // Given
        Long franchiseId = 1L;
        UpdateNameRequest request = new UpdateNameRequest("Updated Name");
        Franchise franchise = new Franchise(franchiseId, "Old Name");
        Franchise updatedFranchise = new Franchise(franchiseId, "Updated Name");

        when(franchiseRepository.findById(franchiseId))
                .thenReturn(Mono.just(franchise));
        when(franchiseRepository.save(any(Franchise.class)))
                .thenReturn(Mono.just(updatedFranchise));

        // When
        Mono<Franchise> result = franchiseService.updateFranchiseName(franchiseId, request);

        // Then
        StepVerifier.create(result)
                .assertNext(f -> {
                    assertThat(f.getName()).isEqualTo("Updated Name");
                })
                .verifyComplete();
    }

    @Test
    void getAllFranchises_shouldReturnAllFranchises() {
        // Given
        Franchise franchise1 = new Franchise(1L, "Franchise 1");
        Franchise franchise2 = new Franchise(2L, "Franchise 2");

        when(franchiseRepository.findAll())
                .thenReturn(Flux.just(franchise1, franchise2));

        // When
        Flux<Franchise> result = franchiseService.getAllFranchises();

        // Then
        StepVerifier.create(result)
                .expectNext(franchise1)
                .expectNext(franchise2)
                .verifyComplete();

        verify(franchiseRepository, times(1)).findAll();
    }

    @Test
    void getTopStockProductsByFranchise_shouldReturnProductsWithBranchInfo() {
        // Given
        Long franchiseId = 1L;
        Franchise franchise = new Franchise(franchiseId, "Test Franchise");
        Product product1 = new Product(1L, "Laptop", 100, 1L);
        Product product2 = new Product(2L, "Mouse", 200, 2L);
        Branch branch1 = new Branch(1L, "Branch 1", franchiseId);
        Branch branch2 = new Branch(2L, "Branch 2", franchiseId);

        when(franchiseRepository.findById(franchiseId))
                .thenReturn(Mono.just(franchise));
        when(productRepository.findTopStockProductsByFranchise(franchiseId))
                .thenReturn(Flux.just(product1, product2));
        when(branchRepository.findById(1L))
                .thenReturn(Mono.just(branch1));
        when(branchRepository.findById(2L))
                .thenReturn(Mono.just(branch2));

        // When
        Flux<ProductResponse> result = franchiseService.getTopStockProductsByFranchise(franchiseId);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getName()).isEqualTo("Laptop");
                    assertThat(response.getStock()).isEqualTo(100);
                    assertThat(response.getBranchName()).isEqualTo("Branch 1");
                })
                .assertNext(response -> {
                    assertThat(response.getName()).isEqualTo("Mouse");
                    assertThat(response.getStock()).isEqualTo(200);
                    assertThat(response.getBranchName()).isEqualTo("Branch 2");
                })
                .verifyComplete();

        verify(franchiseRepository, times(1)).findById(franchiseId);
        verify(productRepository, times(1)).findTopStockProductsByFranchise(franchiseId);
    }

    @Test
    void getTopStockProductsByFranchise_whenFranchiseNotFound_shouldReturnError() {
        // Given
        Long franchiseId = 999L;

        when(franchiseRepository.findById(franchiseId))
                .thenReturn(Mono.empty());

        // When
        Flux<ProductResponse> result = franchiseService.getTopStockProductsByFranchise(franchiseId);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                        throwable.getMessage().contains("Franchise not found"))
                .verify();

        verify(productRepository, never()).findTopStockProductsByFranchise(any());
    }
}