package com.franchise.management.api.controller;

import com.franchise.management.api.dto.*;
import com.franchise.management.api.entity.Branch;
import com.franchise.management.api.entity.Franchise;
import com.franchise.management.api.entity.Product;
import com.franchise.management.api.service.FranchiseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FranchiseControllerTest {

    private WebTestClient webTestClient;

    @Mock
    private FranchiseService franchiseService;

    @BeforeEach
    void setUp() {
        FranchiseController controller = new FranchiseController(franchiseService);
        webTestClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    void createFranchise_shouldReturnCreatedFranchise() {
        // Given
        FranchiseRequest request = new FranchiseRequest("Test Franchise");
        Franchise franchise = new Franchise(1L, "Test Franchise");

        when(franchiseService.createFranchise(any(FranchiseRequest.class)))
                .thenReturn(Mono.just(franchise));

        // When & Then
        webTestClient.post()
                .uri("/api/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Franchise.class)
                .isEqualTo(franchise);
    }

    @Test
    void getAllFranchises_shouldReturnFranchiseList() {
        // Given
        Franchise franchise1 = new Franchise(1L, "Franchise 1");
        Franchise franchise2 = new Franchise(2L, "Franchise 2");

        when(franchiseService.getAllFranchises())
                .thenReturn(Flux.just(franchise1, franchise2));

        // When & Then
        webTestClient.get()
                .uri("/api/franchises")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Franchise.class)
                .hasSize(2)
                .contains(franchise1, franchise2);
    }

    @Test
    void getFranchiseById_shouldReturnFranchiseWithBranches() {
        // Given
        FranchiseResponse response = new FranchiseResponse(1L, "Test Franchise", List.of());

        when(franchiseService.getFranchiseById(1L))
                .thenReturn(Mono.just(response));

        // When & Then
        webTestClient.get()
                .uri("/api/franchises/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(FranchiseResponse.class)
                .isEqualTo(response);
    }

    @Test
    void addBranchToFranchise_shouldReturnCreatedBranch() {
        // Given
        BranchRequest request = new BranchRequest("Test Branch");
        Branch branch = new Branch(1L, "Test Branch", 1L);

        when(franchiseService.addBranchToFranchise(eq(1L), any(BranchRequest.class)))
                .thenReturn(Mono.just(branch));

        // When & Then
        webTestClient.post()
                .uri("/api/franchises/1/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Branch.class)
                .isEqualTo(branch);
    }

    @Test
    void addProductToBranch_shouldReturnCreatedProduct() {
        // Given
        ProductRequest request = new ProductRequest("Laptop", 50);
        Product product = new Product(1L, "Laptop", 50, 1L);

        when(franchiseService.addProductToBranch(eq(1L), any(ProductRequest.class)))
                .thenReturn(Mono.just(product));

        // When & Then
        webTestClient.post()
                .uri("/api/franchises/branches/1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Product.class)
                .isEqualTo(product);
    }

    @Test
    void deleteProduct_shouldReturnNoContent() {
        // Given
        when(franchiseService.deleteProduct(1L))
                .thenReturn(Mono.empty());

        // When & Then
        webTestClient.delete()
                .uri("/api/franchises/products/1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void updateProductStock_shouldReturnUpdatedProduct() {
        // Given
        UpdateStockRequest request = new UpdateStockRequest(100);
        Product product = new Product(1L, "Laptop", 100, 1L);

        when(franchiseService.updateProductStock(eq(1L), any(UpdateStockRequest.class)))
                .thenReturn(Mono.just(product));

        // When & Then
        webTestClient.patch()
                .uri("/api/franchises/products/1/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Product.class)
                .isEqualTo(product);
    }

    @Test
    void getTopStockProductsByFranchise_shouldReturnProductList() {
        // Given
        ProductResponse product1 = new ProductResponse(1L, "Laptop", 100, 1L, "Branch 1");
        ProductResponse product2 = new ProductResponse(2L, "Mouse", 200, 2L, "Branch 2");

        when(franchiseService.getTopStockProductsByFranchise(1L))
                .thenReturn(Flux.just(product1, product2));

        // When & Then
        webTestClient.get()
                .uri("/api/franchises/1/top-stock-products")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponse.class)
                .hasSize(2)
                .contains(product1, product2);
    }
}