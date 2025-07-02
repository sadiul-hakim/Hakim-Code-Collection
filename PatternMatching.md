# Java Pattern Matching (Java 21 to 24)

Pattern matching is a major enhancement in Java to improve the expressiveness and safety of type inspection and data extraction. Between Java 21 and 24, several features related to pattern matching have been introduced or finalized. This document summarizes them by type.

---

## 1. Pattern Matching for `instanceof`

**Available Since:** Java 16 (final), still relevant

```java
if (obj instanceof String s) {
    System.out.println(s.toLowerCase());
}
```

Benefits:

* No need for explicit casting.
* Safer and cleaner code.

---

## 2. Pattern Matching for `switch`

**Previewed in:** Java 17, 18, 19, 20, 21
**Finalized in:** Java 21

### Syntax:

```java
switch (obj) {
    case String s -> System.out.println("String: " + s);
    case Integer i when i > 10 -> System.out.println("Big Integer: " + i);
    default -> System.out.println("Other type");
}
```

### Features:

* Type patterns
* Guarded patterns (using `when`)
* Exhaustiveness checking
* Enhanced readability and safety

---

## 3. Record Patterns

**Previewed in:** Java 19, 20, 21
**Finalized in:** Java 22

### Example:

```java
record Point(int x, int y) {}

void print(Point p) {
    if (p instanceof Point(int x, int y)) {
        System.out.println("x = " + x + ", y = " + y);
    }
}
```

* Supports nested patterns
* Great for deconstructing immutable data classes

### Nested Record Pattern:

```java
record Rectangle(Point topLeft, Point bottomRight) {}

if (r instanceof Rectangle(Point(int x1, int y1), Point(int x2, int y2))) {
    System.out.println("Diagonal from (" + x1 + "," + y1 + ") to (" + x2 + "," + y2 + ")");
}
```

---

## 4. Primitive Patterns (Preview)

**Previewed in:** Java 22
**Refined in:** Java 23 (2nd preview)
**Finalized in:** Java 24 (expected)

### Example:

```java
static void test(Object o) {
    switch (o) {
        case int i -> System.out.println("int: " + i);
        case double d -> System.out.println("double: " + d);
        default -> System.out.println("Other");
    }
}
```

* Allows matching of primitives in type patterns and switch.
* Enhances performance and eliminates unnecessary boxing.

---

## 5. Unnamed Patterns and Variables (Preview)

**Previewed in:** Java 21

```java
if (obj instanceof SomeType(_, _)) {
    System.out.println("Matched but ignoring fields");
}
```

* Use `_` for fields you want to ignore.
* Similar to wildcard in other pattern matching languages.

---

## 6. Dominance Rules for Switch

**Previewed and finalized in:** Java 21

Java enforces dominance rules to prevent unreachable case labels:

```java
switch (obj) {
    case Number n -> System.out.println("Number");
    case Integer i -> System.out.println("Integer"); // Error: unreachable
}
```

Because `Integer` is a subtype of `Number`, the second case is unreachable.

---

## Summary by Java Version

### Java 21 (Finalized):

* Pattern Matching for switch
* Record patterns (3rd preview)
* Dominance checking
* Unnamed patterns and variables (preview)

### Java 22:

* Record patterns (final)
* Primitive patterns (1st preview)
* Enhanced unnamed patterns

### Java 23:

* Primitive patterns (2nd preview)

### Java 24 (expected):

* Primitive patterns (final)

---

## Benefits of Pattern Matching in Java

* Better readability and concise syntax
* Safer type casting
* Cleaner deconstruction of data objects
* Brings Java closer to modern pattern-matching languages like Scala and Kotlin

---

## References

* [JEP 394](https://openjdk.org/jeps/394): Pattern Matching for `instanceof`
* [JEP 406](https://openjdk.org/jeps/406): Pattern Matching for switch (Preview)
* [JEP 433](https://openjdk.org/jeps/433): Pattern Matching for switch (Final)
* [JEP 405](https://openjdk.org/jeps/405): Record Patterns
* [JEP 440, 441, 442, 443](https://openjdk.org): Primitive and unnamed patterns
