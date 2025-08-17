
# 🔍 Java Reflection API — A Comprehensive Tutorial

## 1. What is Reflection?

Reflection in Java is a feature that lets your code **inspect and manipulate classes, methods, fields, and constructors at runtime** — even if you don’t know their names at compile time.

It’s often used in:

* Frameworks (Spring, Hibernate, JUnit)
* Dependency injection
* Annotation processing
* Serialization/Deserialization
* Debugging and testing tools

⚠️ **Caution:** Reflection breaks encapsulation and can hurt performance if abused. Use only when necessary.

---

## 2. Core Classes in Reflection

All in `java.lang.reflect` and `java.lang.Class`.

### 2.1 `Class<?>`

* The entry point for Reflection.
* Every object has a `Class` instance at runtime.

```java
Class<?> clazz = String.class;                // using .class
Class<?> clazz2 = "hello".getClass();         // using object
Class<?> clazz3 = Class.forName("java.util.Date"); // dynamic load
```

**Useful methods:**

* `getName()`, `getSimpleName()`
* `getPackage()`
* `getSuperclass()`
* `getInterfaces()`
* `getDeclaredMethods()`, `getDeclaredFields()`, `getDeclaredConstructors()`
* `newInstance()` (deprecated, use `clazz.getDeclaredConstructor().newInstance()`)

---

### 2.2 `Field`

Represents a field (variable) of a class.

```java
import java.lang.reflect.*;

class Person {
    private String name = "John";
}

public class FieldExample {
    public static void main(String[] args) throws Exception {
        Person p = new Person();
        Class<?> clazz = p.getClass();

        Field field = clazz.getDeclaredField("name");
        field.setAccessible(true); // bypass private access

        String value = (String) field.get(p);
        System.out.println("Field value: " + value);

        field.set(p, "Alice");
        System.out.println("Updated: " + field.get(p));
    }
}
```

**Useful methods:**

* `getName()`
* `getType()`
* `getModifiers()`
* `get(Object obj)` → get value
* `set(Object obj, Object value)` → set value

---

### 2.3 `Method`

Represents a method of a class.

```java
import java.lang.reflect.*;

class Calculator {
    public int add(int a, int b) { return a + b; }
}

public class MethodExample {
    public static void main(String[] args) throws Exception {
        Calculator calc = new Calculator();
        Class<?> clazz = calc.getClass();

        Method method = clazz.getDeclaredMethod("add", int.class, int.class);
        Object result = method.invoke(calc, 5, 3);

        System.out.println("Result: " + result);
    }
}
```

**Useful methods:**

* `getName()`
* `getReturnType()`
* `getParameterTypes()`
* `getModifiers()`
* `invoke(Object obj, Object... args)`

---

### 2.4 `Constructor<T>`

Represents a constructor of a class.

```java
import java.lang.reflect.*;

class User {
    private String username;
    public User(String username) { this.username = username; }
}

public class ConstructorExample {
    public static void main(String[] args) throws Exception {
        Class<?> clazz = User.class;
        Constructor<?> ctor = clazz.getConstructor(String.class);
        Object user = ctor.newInstance("Sadiul");

        System.out.println("User created: " + user);
    }
}
```

**Useful methods:**

* `getName()`
* `getParameterTypes()`
* `newInstance(Object... args)`

---

### 2.5 `Modifier`

A utility class to decode field/method/constructor modifiers.

```java
import java.lang.reflect.*;

class Sample {
    public static final int number = 42;
}

public class ModifierExample {
    public static void main(String[] args) throws Exception {
        Field f = Sample.class.getDeclaredField("number");
        int mod = f.getModifiers();

        System.out.println(Modifier.isPublic(mod));   // true
        System.out.println(Modifier.isStatic(mod));   // true
        System.out.println(Modifier.isFinal(mod));    // true
    }
}
```

---

### 2.6 `Parameter`

Represents a method/constructor parameter.

```java
import java.lang.reflect.*;

class Greeter {
    public void greet(String message, int count) {}
}

public class ParameterExample {
    public static void main(String[] args) throws Exception {
        Method m = Greeter.class.getDeclaredMethod("greet", String.class, int.class);
        Parameter[] params = m.getParameters();

        for (Parameter p : params) {
            System.out.println("Param: " + p.getName() + " Type: " + p.getType());
        }
    }
}
```

---

### 2.7 `Annotation`

Reflection lets you **read annotations at runtime**.

```java
import java.lang.annotation.*;
import java.lang.reflect.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Info {
    String author();
}

@Info(author = "Hakim")
class Demo {}

public class AnnotationExample {
    public static void main(String[] args) {
        Class<?> clazz = Demo.class;
        Info info = clazz.getAnnotation(Info.class);
        System.out.println("Author: " + info.author());
    }
}
```

---

## 3. Common Use Cases

1. **Frameworks & Dependency Injection**
   Load classes dynamically, call methods without compile-time knowledge (Spring, Hibernate).

2. **Object Mappers**
   Libraries like Jackson use reflection to read fields/annotations and serialize/deserialize.

3. **Testing Tools**
   JUnit uses reflection to find and execute test methods with `@Test`.

4. **Annotation Processing**
   Custom annotations like `@Entity`, `@Autowired`.

---

## 4. Best Practices

✅ **Use reflection only when necessary**
Prefer normal method/field access when you can.

✅ **Cache reflection results**
Expensive operations like `getDeclaredMethods()` should be cached in frameworks.

✅ **Use `setAccessible(true)` sparingly**
It breaks encapsulation and may cause security risks.

✅ **Prefer annotations over naming conventions**
Instead of searching for `"getName"`, mark methods with `@Getter`.

✅ **Be aware of performance**
Reflection is slower than direct access. Avoid inside tight loops.

✅ **Security considerations**
In restricted environments, `setAccessible(true)` may fail due to `SecurityManager`.

---

## 5. Advanced Tip — Dynamic Proxies

Reflection is often paired with `java.lang.reflect.Proxy` to create runtime implementations of interfaces.

```java
import java.lang.reflect.*;

interface Service {
    void execute();
}

class ProxyDemo {
    public static void main(String[] args) {
        Service service = (Service) Proxy.newProxyInstance(
            Service.class.getClassLoader(),
            new Class[]{Service.class},
            (proxy, method, methodArgs) -> {
                System.out.println("Before method");
                Object result = null; // no actual impl
                System.out.println("After method");
                return result;
            }
        );

        service.execute();
    }
}
```

---

✅ That’s the **big picture**: you now know the **core reflection classes, methods, use cases, examples, and best practices**.

---