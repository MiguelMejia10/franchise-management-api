package com.franchise.management.api.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table("branches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Branch {

    @Id
    private Long id;

    private String name;

    @Column("franchise_id")
    private Long franchiseId;
}