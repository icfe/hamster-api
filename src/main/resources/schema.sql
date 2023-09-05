CREATE TABLE stores(
    store_id INT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    owner VARCHAR(30) NOT NULL
);
CREATE SEQUENCE store_id_seq;
CREATE TABLE products(
    store_id INT NOT NULL REFERENCES stores(store_id),
    product_id INT PRIMARY KEY,
    product_price DECIMAL(10,2) NOT NULL,
    quantity INTEGER NOT NULL,
    post_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    product_desc VARCHAR(1024) NOT NULL

);
CREATE SEQUENCE product_id_seq;
CREATE INDEX post_timestamp_idx ON products(post_time);
CREATE UNIQUE INDEX store_name_idx ON stores(name);

CREATE USER hamster_api_user PASSWORD 'password';
GRANT SELECT, INSERT ON stores, products TO hamster_api_user;
GRANT DELETE ON products TO hamster_api_user;