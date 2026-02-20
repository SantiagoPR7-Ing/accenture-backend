# accenture-backend
API REST desarrollada en Spring Boot para gestionar franquicias, sucursales y productos con control de stock. Permite crear, actualizar y eliminar entidades, además de consultar el producto con mayor inventario por sucursal dentro de una franquicia. Incluye persistencia de datos y soporte para despliegue con Docker.

```mermaid
erDiagram
    FRANCHISE ||--o{ BRANCH : has
    BRANCH ||--o{ BRANCH_PRODUCT : offers
    PRODUCT ||--o{ BRANCH_PRODUCT : listed_as

    FRANCHISE {
        bigint id PK
        varchar name UK
        timestamptz created_at
        timestamptz updated_at
    }

    BRANCH {
        bigint id PK
        bigint franchise_id FK
        varchar name
        timestamptz created_at
        timestamptz updated_at
    }

    PRODUCT {
        bigint id PK
        varchar name UK
        timestamptz created_at
        timestamptz updated_at
    }

    BRANCH_PRODUCT {
        bigint id PK
        bigint branch_id FK
        bigint product_id FK
        int stock
        timestamptz created_at
        timestamptz updated_at
    }

```
