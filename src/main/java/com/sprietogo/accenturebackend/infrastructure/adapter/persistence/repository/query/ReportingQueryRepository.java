package com.sprietogo.accenturebackend.infrastructure.adapter.persistence.repository.query;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;


@Repository
public class ReportingQueryRepository {

    private final DatabaseClient db;

    public ReportingQueryRepository(DatabaseClient db) {
        this.db = db;
    }


    public Flux<TopStockProductByBranchRow> findTopStockProductsByBranch(Long franchiseId) {
        final String sql = """
        SELECT
          b.id   AS branch_id,
          b.name AS branch_name,
          p.id   AS product_id,
          p.name AS product_name,
          x.stock AS stock
        FROM (
          SELECT
            bp.branch_id,
            bp.product_id,
            bp.stock,
            ROW_NUMBER() OVER (
              PARTITION BY bp.branch_id
              ORDER BY bp.stock DESC, bp.product_id ASC
            ) AS rn
          FROM branch_product bp
        ) x
        JOIN branch b  ON b.id = x.branch_id
        JOIN product p ON p.id = x.product_id
        WHERE b.franchise_id = $1
          AND x.rn = 1
        ORDER BY b.id
        """;

        return db.sql(sql)
                .bind(0, franchiseId)
                .map((row, meta) -> new TopStockProductByBranchRow(
                        row.get("branch_id", Long.class),
                        row.get("branch_name", String.class),
                        row.get("product_id", Long.class),
                        row.get("product_name", String.class),
                        row.get("stock", Integer.class)
                ))
                .all();
    }

    public Flux<TopStockProductByBranchRow> findTopStockProductsByBranchWithTies(Long franchiseId) {
        final String sql = """
        SELECT
          b.id   AS branch_id,
          b.name AS branch_name,
          p.id   AS product_id,
          p.name AS product_name,
          bp.stock AS stock
        FROM branch_product bp
        JOIN branch b  ON b.id = bp.branch_id
        JOIN product p ON p.id = bp.product_id
        JOIN (
          SELECT bp2.branch_id, MAX(bp2.stock) AS max_stock
          FROM branch_product bp2
          JOIN branch b2 ON b2.id = bp2.branch_id
          WHERE b2.franchise_id = $1
          GROUP BY bp2.branch_id
        ) mx ON mx.branch_id = bp.branch_id AND mx.max_stock = bp.stock
        WHERE b.franchise_id = $2
        ORDER BY b.id, p.id
        """;

        return db.sql(sql)
                .bind(0, franchiseId) // $1
                .bind(1, franchiseId) // $2
                .map((row, meta) -> new TopStockProductByBranchRow(
                        row.get("branch_id", Long.class),
                        row.get("branch_name", String.class),
                        row.get("product_id", Long.class),
                        row.get("product_name", String.class),
                        row.get("stock", Integer.class)
                ))
                .all();
    }

    public record TopStockProductByBranchRow(
            Long branchId,
            String branchName,
            Long productId,
            String productName,
            Integer stock
    ) {}

}