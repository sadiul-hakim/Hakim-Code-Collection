PostgreSQL has powerful support for JSON and JSONB (binary JSON) data types. You can store, query, and manipulate JSON data directly in your database.

---

## 🔧 Prerequisites

* PostgreSQL 9.2+ for basic JSON
* PostgreSQL 9.4+ recommended for JSONB (better indexing and performance)

---

## 📘 JSON vs JSONB

| JSON                             | JSONB                                         |
| -------------------------------- | --------------------------------------------- |
| Text format (stored as-is)       | Binary format (parsed and stored efficiently) |
| Preserves key order & formatting | Does not preserve order or duplicate keys     |
| Slower for querying              | Faster for querying                           |
| Good for logging                 | Good for indexing & search                    |

---

## 🗃️ 1. Creating Tables with JSON/JSONB

```sql
CREATE TABLE products (
  id SERIAL PRIMARY KEY,
  name TEXT,
  attributes JSONB
);
```

---

## 📥 2. Inserting JSON Data

```sql
INSERT INTO products (name, attributes) VALUES
('Laptop', '{"brand": "Dell", "specs": {"ram": "16GB", "cpu": "i7"}}'),
('Phone', '{"brand": "Samsung", "specs": {"ram": "8GB", "cpu": "Exynos"}}');
```

---

## 🔎 3. Querying JSON Data

### a. Get all rows where brand is "Dell"

```sql
SELECT * FROM products
WHERE attributes->>'brand' = 'Dell';
```

### b. Get nested value: CPU of each product

```sql
SELECT 
  name,
  attributes->'specs'->>'cpu' AS cpu
FROM products;
```

---

## 🛠️ 4. Updating JSON Data

### Add a new key-value to JSON

```sql
UPDATE products
SET attributes = jsonb_set(attributes, '{price}', '"1200"', true)
WHERE name = 'Laptop';
```

### Change nested value

```sql
UPDATE products
SET attributes = jsonb_set(attributes, '{specs,ram}', '"32GB"', true)
WHERE name = 'Laptop';
```

---

## 🔍 5. Filtering Using JSON Keys

### Check if a key exists

```sql
SELECT * FROM products
WHERE attributes ? 'brand';
```

### Check if multiple keys exist

```sql
SELECT * FROM products
WHERE attributes ?& array['brand', 'specs'];
```

---

## 📊 6. Indexing JSONB Fields

### a. GIN Index for faster key lookups

```sql
CREATE INDEX idx_attributes ON products USING gin (attributes jsonb_path_ops);
```

### b. Example Query Using Index

```sql
SELECT * FROM products
WHERE attributes @> '{"brand": "Dell"}';
```

---

## 🧪 7. Advanced: JSON Aggregation

### Group by brand, count products

```sql
SELECT 
  attributes->>'brand' AS brand,
  COUNT(*) AS count
FROM products
GROUP BY attributes->>'brand';
```

---

## 🔁 8. Extracting JSON Arrays

```sql
-- Suppose a product has multiple tags
UPDATE products
SET attributes = jsonb_set(attributes, '{tags}', '["electronics", "computers"]')
WHERE name = 'Laptop';

-- Unnest array
SELECT 
  name,
  jsonb_array_elements_text(attributes->'tags') AS tag
FROM products;
```

---

## 🧼 9. Deleting JSON Keys

```sql
-- Remove the "price" field from attributes
UPDATE products
SET attributes = attributes - 'price'
WHERE name = 'Laptop';
```

---

## 🧰 Bonus: Useful Operators

| Operator | Example                     | Description            |                   |             |
| -------- | --------------------------- | ---------------------- | ----------------- | ----------- |
| `->`     | `json->'key'`               | Returns JSON object    |                   |             |
| `->>`    | `json->>'key'`              | Returns text           |                   |             |
| `#>`     | `json#>'{a,b}'`             | Get nested JSON object |                   |             |
| `#>>`    | `json#>>'{a,b}'`            | Get nested text        |                   |             |
| `@>`     | `jsonb @> '{"a":1}'`        | Contains               |                   |             |
| `<@`     | `jsonb <@ '{"a":1, "b":2}'` | Is contained by        |                   |             |
| `?`      | `jsonb ? 'key'`             | Has key                |                   |             |
| \`?      | \`                          | \`jsonb ?              | array\['a','b']\` | Has any key |
| `?&`     | `jsonb ?& array['a','b']`   | Has all keys           |                   |             |

---

## 💡 Best Practices

* Prefer `JSONB` for querying, indexing, performance
* Use `JSON` if you need to preserve formatting or key order
* Index only if you frequently query JSON fields
* Avoid deeply nested structures unless necessary

---
