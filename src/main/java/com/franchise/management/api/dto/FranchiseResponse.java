package com.franchise.management.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FranchiseResponse {
    private Long id;
    private String name;
    private List<BranchResponse> branches;
}