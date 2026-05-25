package com.franchise.management.api.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table("franchises")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Franchise {

    @Id
    private Long id;

    private String name;
}