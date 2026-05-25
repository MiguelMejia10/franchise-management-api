package com.franchise.management.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock must be greater than or equal to 0")
    private Integer stock;
}