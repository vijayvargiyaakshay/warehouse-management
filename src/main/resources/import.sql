INSERT INTO store(id, name, quantityProductsInStock) VALUES (1, 'TONSTAD', 10);
INSERT INTO store(id, name, quantityProductsInStock) VALUES (2, 'KALLAX', 5);
INSERT INTO store(id, name, quantityProductsInStock) VALUES (3, 'BESTÅ', 3);
ALTER SEQUENCE store_seq RESTART WITH 4;

INSERT INTO product(id, name, stock) VALUES (1, 'TONSTAD', 10);
INSERT INTO product(id, name, stock) VALUES (2, 'KALLAX', 5);
INSERT INTO product(id, name, stock) VALUES (3, 'BESTÅ', 3);
ALTER SEQUENCE product_seq RESTART WITH 4;

INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt) 
VALUES (1, 'MWH.001', 'ZWOLLE-001', 100, 10, '2024-07-01', null);
INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt)
VALUES (2, 'MWH.012', 'AMSTERDAM-001', 50, 5, '2023-07-01', null);
INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt)
VALUES (3, 'MWH.023', 'TILBURG-001', 30, 27, '2021-02-01', null);

-- Extra ACTIVE warehouses used only by the fulfilment tests. They sit in an isolated
-- 'FULFIL-ZONE' location so they do not affect the warehouse-creation location limits.
INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt) VALUES (10, 'FUL.001', 'FULFIL-ZONE', 100, 0, '2024-01-01', null);
INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt) VALUES (11, 'FUL.002', 'FULFIL-ZONE', 100, 0, '2024-01-01', null);
INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt) VALUES (12, 'FUL.003', 'FULFIL-ZONE', 100, 0, '2024-01-01', null);
INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt) VALUES (13, 'FUL.004', 'FULFIL-ZONE', 100, 0, '2024-01-01', null);
INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt) VALUES (14, 'FUL.005', 'FULFIL-ZONE', 100, 0, '2024-01-01', null);
INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt) VALUES (15, 'FUL.006', 'FULFIL-ZONE', 100, 0, '2024-01-01', null);
INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt) VALUES (16, 'FUL.007', 'FULFIL-ZONE', 100, 0, '2024-01-01', null);
INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt) VALUES (17, 'FUL.008', 'FULFIL-ZONE', 100, 0, '2024-01-01', null);
INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt) VALUES (18, 'FUL.009', 'FULFIL-ZONE', 100, 0, '2024-01-01', null);
INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt) VALUES (19, 'FUL.010', 'FULFIL-ZONE', 100, 0, '2024-01-01', null);
ALTER SEQUENCE warehouse_seq RESTART WITH 20;
