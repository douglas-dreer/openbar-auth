CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    price DECIMAL(10, 2) NOT NULL,
    category_id UUID NOT NULL REFERENCES categories(id),
    routing VARCHAR(20) NOT NULL CHECK (routing IN ('KITCHEN', 'COUNTER')),
    active BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_active ON products(active);
CREATE INDEX idx_products_routing ON products(routing);
