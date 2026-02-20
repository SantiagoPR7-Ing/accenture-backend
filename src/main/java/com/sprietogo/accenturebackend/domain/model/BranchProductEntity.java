package com.sprietogo.accenturebackend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Table("branch_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BranchProductEntity {

    @Id
    private Long id;

    @Column("branch_id")
    private Long branchId;

    @Column("product_id")
    private Long productId;

    @Column("stock")
    private Integer stock;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;

}