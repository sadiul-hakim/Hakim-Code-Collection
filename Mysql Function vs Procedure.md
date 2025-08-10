In MySQL, **procedures** and **functions** are both stored programs, but they serve different purposes and have different rules.

Here’s the breakdown:

---

## **1. Purpose**

| Feature      | **Stored Procedure**                                          | **Function**                                 |
| ------------ | ------------------------------------------------------------- | -------------------------------------------- |
| Main use     | Perform actions (e.g., insert, update, delete, complex logic) | Compute and return a single value            |
| Called from  | `CALL` statement                                              | SQL expressions (e.g., `SELECT myfunc(...)`) |
| Side effects | Can modify data (INSERT, UPDATE, DELETE, CREATE TABLE, etc.)  | Should not modify data (only read)           |

---

## **2. Syntax & Calling**

**Procedure**

```sql
DELIMITER //
CREATE PROCEDURE AddUser(IN username VARCHAR(50))
BEGIN
    INSERT INTO users(name) VALUES(username);
END //
DELIMITER ;

CALL AddUser('John');
```

**Function**

```sql
DELIMITER //
CREATE FUNCTION FullName(first VARCHAR(50), last VARCHAR(50))
RETURNS VARCHAR(101)
DETERMINISTIC
BEGIN
    RETURN CONCAT(first, ' ', last);
END //
DELIMITER ;

SELECT FullName('John', 'Doe');
```

---

## **3. Key Differences Table**

| Feature                                        | Procedure                             | Function                                              |
| ---------------------------------------------- | ------------------------------------- | ----------------------------------------------------- |
| **Return value**                               | No direct return (can use OUT params) | Must return exactly **one** value                     |
| **Call syntax**                                | `CALL proc_name(...)`                 | Inside SQL: `SELECT func_name(...)`                   |
| **Can modify data**                            | Yes                                   | Not allowed (unless using special settings)           |
| **Parameters**                                 | `IN`, `OUT`, `INOUT`                  | Only `IN`                                             |
| **Transaction control** (`COMMIT`, `ROLLBACK`) | Allowed                               | Not allowed                                           |
| **Use in SQL queries**                         | No                                    | Yes                                                   |
| **Deterministic**                              | Not required                          | Should declare `DETERMINISTIC` or `NOT DETERMINISTIC` |
| **Side effects**                               | Can have many                         | Should be side-effect-free                            |

---

💡 **Rule of thumb:**

* Use a **procedure** if you want to *do something* (e.g., change data, perform multiple operations).
* Use a **function** if you want to *calculate something* and use it directly in queries.

---