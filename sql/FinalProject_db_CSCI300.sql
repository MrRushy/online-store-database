
-- CREATE TABLES 

-- 1) Use the database
USE online_store_db;

-- 2) Customers table
CREATE TABLE customers (
    customer_id      INT AUTO_INCREMENT PRIMARY KEY,
    username         VARCHAR(50) NOT NULL UNIQUE,
    password         VARCHAR(255) NOT NULL,
    full_name        VARCHAR(100) NOT NULL,
    address_line1    VARCHAR(255) NOT NULL,
    city             VARCHAR(100),
    state            VARCHAR(100),
    postal_code      VARCHAR(20),
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3) Employees table
CREATE TABLE employees (
    employee_id      INT AUTO_INCREMENT PRIMARY KEY,
    username         VARCHAR(50) NOT NULL UNIQUE,
    password         VARCHAR(255) NOT NULL,
    full_name        VARCHAR(100) NOT NULL,
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4) Admins table
CREATE TABLE admins (
    admin_id         INT AUTO_INCREMENT PRIMARY KEY,
    username         VARCHAR(50) NOT NULL UNIQUE,
    password         VARCHAR(255) NOT NULL,
    full_name        VARCHAR(100) NOT NULL,
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 5) Items table (products in the store)
CREATE TABLE items (
    item_id           INT AUTO_INCREMENT PRIMARY KEY,
    item_name         VARCHAR(150) NOT NULL,
    description       TEXT,
    price             DECIMAL(10,2) NOT NULL,
    quantity_in_stock INT NOT NULL DEFAULT 0,
    is_active         TINYINT(1) NOT NULL DEFAULT 1,
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 6) Coupons table (optional feature but good to have)
CREATE TABLE coupons (
    coupon_id        INT AUTO_INCREMENT PRIMARY KEY,
    code             VARCHAR(50) NOT NULL UNIQUE,
    discount_type    ENUM('PERCENT', 'AMOUNT') NOT NULL,
    discount_value   DECIMAL(10,2) NOT NULL,
    is_active        TINYINT(1) NOT NULL DEFAULT 1,
    valid_from       DATE,
    valid_to         DATE
);

-- 7) Orders table
CREATE TABLE orders (
    order_id         INT AUTO_INCREMENT PRIMARY KEY,
    customer_id      INT NOT NULL,
    order_date       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status           ENUM('PENDING', 'PROCESSING', 'SHIPPED', 'CANCELLED', 'COMPLETED')
                     NOT NULL DEFAULT 'PENDING',
    total_amount     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    coupon_id        INT,
    shipping_address VARCHAR(255),
    CONSTRAINT fk_orders_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(customer_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_orders_coupon
        FOREIGN KEY (coupon_id)
        REFERENCES coupons(coupon_id)
        ON DELETE SET NULL
);

-- 8) Order items (line items for each order)
CREATE TABLE order_items (
    order_item_id    INT AUTO_INCREMENT PRIMARY KEY,
    order_id         INT NOT NULL,
    item_id          INT NOT NULL,
    quantity         INT NOT NULL,
    unit_price       DECIMAL(10,2) NOT NULL,  -- price at time of purchase
    line_total       DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES orders(order_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_order_items_item
        FOREIGN KEY (item_id)
        REFERENCES items(item_id)
        ON DELETE RESTRICT
);


-- INSERT QUERIES
USE online_store_db;
-- 1) Admins
INSERT INTO admins (username, password, full_name)
VALUES
    ('levi_admin',   'password123', 'Levi Ackerman'),
    ('gojo_admin',   'limitless99', 'Satoru Gojo'),
    ('kaguya_admin', 'loveiswar1',  'Kaguya Shinomiya');
    
-- 2) Employees
INSERT INTO employees (username, password, full_name)
VALUES
    ('naruto_emp',  'ramen123',   'Naruto Uzumaki'),
    ('mikasa_emp',  'titanblade', 'Mikasa Ackerman'),
    ('ichigo_emp',  'bankai777',  'Ichigo Kurosaki'),
    ('dio_emp',   'zawarudo',  'Dio Brando'),
    ('spike_emp',   'bebop999',   'Spike Spiegel');

-- 3) Customers

INSERT INTO customers (username, password, full_name,
                       address_line1, city, state, postal_code)
VALUES
    ('jotaro_cust', 'thestar17',  'Jotaro Kujo',
     '123 Mummy Lane', 'Egypt',     'Kario',   '100-0001'),
    ('zoro_cust',  '3swordstyle',   'Roronoa Zoro',
     '456 Bamboo St',   'Kyoto',     'Kyoto',   '600-0001'),
    ('deku_cust',    'oneforall',  'Izuku Midoriya',
     '1 Heroes Ave',    'Musutafu',  'Aichi',   '460-0001'),
    ('rengoku_cust', 'flamehash',  'Kyojuro Rengoku',
     '9 Flame Way',     'Osaka',     'Osaka',   '530-0001'),
    ('hinata_cust',  'byakugan8',  'Hinata Hyuga',
     '78 Leaf Village Rd', 'Konoha', 'Fire',    '000-0001');
     
-- 4) Items
INSERT INTO items (item_name, description, price, quantity_in_stock, is_active)
VALUES
    ('Gaming Laptop',
     '15.6-inch gaming laptop with RTX graphics and 16GB RAM.',
     1299.99, 10, 1),

    ('Mechanical Keyboard',
     'RGB backlit mechanical keyboard with blue switches.',
     89.99, 25, 1),

    ('Wireless Gaming Mouse',
     'Ergonomic wireless mouse with adjustable DPI.',
     49.99, 40, 1),

    ('Noise-Cancelling Headphones',
     'Over-ear headphones with active noise cancellation.',
     199.99, 15, 1),

    ('27-inch Monitor',
     '27-inch 144Hz IPS gaming monitor, 1ms response time.',
     279.99, 8, 1),

    ('Video Game Figure - Malenia Blade of Miqulla',
     'Limited Edition Figure from the Collectors Release of Elden Ring',
     159.99, 30, 1),

    ('USB-C Hub',
     '7-in-1 USB-C hub with HDMI and card reader.',
     39.99, 50, 1),

    ('External SSD 1TB',
     'Portable 1TB external SSD with USB-C connection.',
     119.99, 20, 1);
     
-- 5) Coupons 
USE online_store_db;
INSERT INTO coupons (code, discount_type, discount_value, is_active, valid_from, valid_to)
VALUES
    ('WELCOME10', 'PERCENT', 10.00, 1, '2025-01-01', '2025-12-31'),
    ('FREESHIP',  'AMOUNT',  15.00, 1, '2025-02-01', '2025-12-31'),
    ('SPRING20',  'PERCENT', 20.00, 1, '2025-03-01', '2025-06-30'),
    ('CLEAR50',   'PERCENT', 50.00, 0, '2024-01-01', '2024-12-31');  -- example of an inactive/expired coupon









