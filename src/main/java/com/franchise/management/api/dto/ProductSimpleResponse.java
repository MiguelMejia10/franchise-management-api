package com.franchise.management.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSimpleResponse {
    private Long id;
    private String name;
    private Integer stock;
}