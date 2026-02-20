CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE franchise (
                           id BIGSERIAL PRIMARY KEY,
                           name VARCHAR(120) NOT NULL UNIQUE,
                           created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                           updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_franchise_updated_at
    BEFORE UPDATE ON franchise
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE branch (
                        id BIGSERIAL PRIMARY KEY,
                        franchise_id BIGINT NOT NULL REFERENCES franchise(id) ON DELETE RESTRICT,
                        name VARCHAR(120) NOT NULL,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        CONSTRAINT uk_branch_name_per_franchise UNIQUE (franchise_id, name)
);

CREATE INDEX idx_branch_franchise_id ON branch(franchise_id);

CREATE TRIGGER trg_branch_updated_at
    BEFORE UPDATE ON branch
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE product (
                         id BIGSERIAL PRIMARY KEY,
                         name VARCHAR(120) NOT NULL UNIQUE,
                         created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                         updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_product_updated_at
    BEFORE UPDATE ON product
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE branch_product (
                                id BIGSERIAL PRIMARY KEY,
                                branch_id BIGINT NOT NULL REFERENCES branch(id) ON DELETE CASCADE,
                                product_id BIGINT NOT NULL REFERENCES product(id) ON DELETE RESTRICT,
                                stock INT NOT NULL CHECK (stock >= 0),
                                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                CONSTRAINT uk_bp_branch_product UNIQUE (branch_id, product_id)
);

CREATE INDEX idx_bp_branch_stock ON branch_product(branch_id, stock DESC);
CREATE INDEX idx_bp_product_id ON branch_product(product_id);

CREATE TRIGGER trg_branch_product_updated_at
    BEFORE UPDATE ON branch_product
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

