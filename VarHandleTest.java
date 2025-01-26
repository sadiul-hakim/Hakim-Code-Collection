/*
  Java's VarHandle is a part of the java.lang.invoke package and provides a powerful, low-level mechanism to perform variable access operations (like get, set, compareAndSet, etc.)
  in a safe and efficient way. It was introduced in Java 9 as a replacement for sun.misc.Unsafe and is used for operations on fields, array elements, or off-heap memory segments.

  1. Key Features of VarHandle
  a. Low-Level Access: Directly manipulate variables in a more controlled manner.
  b. Atomic Operations: Includes atomic operations like compareAndSet, getAndSet, etc.
  c. Thread-Safety: Facilitates thread-safe access and updates.
  d. Type-Safety: Enforces type safety during access and updates.
  e. Works with Arrays and Fields: Can operate on both array elements and fields of objects.

 2. Common Use Cases
 a. Concurrent programming (atomic updates).
 b. Manipulating fields, static fields, and array elements efficiently.
 c. Working with off-heap memory in conjunction with MemorySegment.

  Creating and Using a VarHandle
  A VarHandle is typically obtained via a factory method, depending on what you want to manipulate (e.g., fields, array elements).

3. Atomic Operations with VarHandle
VarHandle provides a variety of atomic methods, such as:

a. getAndSet: Atomically sets a value and returns the previous value.
b. compareAndSet: Performs a CAS operation.
c. getAndAdd: Atomically adds a value and returns the previous value.

4. Why Use VarHandle?
a. Modern Alternative: Replaces sun.misc.Unsafe in most cases.
b. Performance: Provides efficient variable access and updates.
c. Flexibility: Supports fields, static fields, arrays, and native memory.
d. If you need deeper insights or help with a specific use case, let me know!
*/

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

class VarHandleExample {
    private int value;

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        VarHandle handle = MethodHandles.lookup().findVarHandle(VarHandleExample.class, "value", int.class);

        VarHandleExample example = new VarHandleExample();

        // Set value using VarHandle
        handle.set(example, 42);

        // Get value using VarHandle
        int currentValue = (int) handle.get(example);

        System.out.println("Value: " + currentValue);

        // Compare-and-set (CAS) operation
        boolean updated = handle.compareAndSet(example, 42, 99);

        System.out.println("CAS successful: " + updated);
        System.out.println("Updated Value: " + handle.get(example));
    }
}

class StaticVarHandleExample {
    private static int staticValue;

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        VarHandle handle = MethodHandles.lookup().findStaticVarHandle(StaticVarHandleExample.class, "staticValue", int.class);

        // Set value
        handle.set(123);

        // Get value
        System.out.println("Static Value: " + handle.get());

        // Increment using getAndAdd
        int oldValue = (int) handle.getAndAdd(10);

        System.out.println("Old Value: " + oldValue);
        System.out.println("New Value: " + handle.get());
    }
}

class ArrayVarHandleExample {
    public static void main(String[] args) {
        int[] array = {1, 2, 3, 4, 5};

        // Get a VarHandle for array elements
        VarHandle handle = MethodHandles.arrayElementVarHandle(int[].class);

        // Get the value at index 2
        int value = (int) handle.get(array, 2);
        System.out.println("Value at index 2: " + value);

        // Set a new value at index 2
        handle.set(array, 2, 99);
        System.out.println("Updated value at index 2: " + array[2]);

        // Atomic compare-and-set (CAS) at index 2
        boolean updated = handle.compareAndSet(array, 2, 99, 42);
        System.out.println("CAS successful: " + updated);
        System.out.println("Value after CAS: " + array[2]);
    }
}
