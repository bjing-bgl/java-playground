# Java Function Types Reference

## Built-in Functional Interfaces

Java provides several functional interfaces in `java.util.function`. Each represents a different shape of function.

| Interface | Signature | Description |
|---|---|---|
| `Function<T, R>` | `T → R` | Takes one argument, returns a result |
| `BiFunction<T, U, R>` | `(T, U) → R` | Takes two arguments, returns a result |
| `Predicate<T>` | `T → boolean` | Tests a condition, returns true/false |
| `BiPredicate<T, U>` | `(T, U) → boolean` | Tests two arguments |
| `Consumer<T>` | `T → void` | Takes an argument, returns nothing (side effect) |
| `BiConsumer<T, U>` | `(T, U) → void` | Takes two arguments, returns nothing |
| `Supplier<T>` | `() → T` | Takes no argument, returns a result |
| `UnaryOperator<T>` | `T → T` | Takes and returns the same type |
| `BinaryOperator<T>` | `(T, T) → T` | Takes two of the same type, returns same type |

### Primitive Specializations (avoid boxing overhead)

| Interface | Signature |
|---|---|
| `IntFunction<R>` | `int → R` |
| `ToIntFunction<T>` | `T → int` |
| `IntUnaryOperator` | `int → int` |
| `IntBinaryOperator` | `(int, int) → int` |
| `IntSupplier` | `() → int` |
| `IntConsumer` | `int → void` |
| `IntPredicate` | `int → boolean` |

Same pattern exists for `Long` and `Double` variants.

---

## Usage Examples

```java
import java.util.function.*;

// Function<T, R> — transform a value
Function<String, Integer> strLen = String::length;
int len = strLen.apply("hello"); // 5

// Predicate<T> — test a condition
Predicate<String> isBlank = String::isBlank;
boolean blank = isBlank.test(""); // true

// Consumer<T> — side effect, no return
Consumer<String> printer = System.out::println;
printer.accept("hello"); // prints "hello"

// Supplier<T> — produce a value, no input
Supplier<List<String>> listFactory = ArrayList::new;
List<String> list = listFactory.get(); // new ArrayList

// UnaryOperator<T> — same type in and out
UnaryOperator<String> shout = s -> s.toUpperCase() + "!";
String result = shout.apply("hello"); // "HELLO!"

// BinaryOperator<T> — combine two of the same type
BinaryOperator<Integer> add = Integer::sum;
int sum = add.apply(3, 4); // 7
```

---

## Defining Custom Functional Interfaces

Use `@FunctionalInterface` when the built-in types don't fit your shape (wrong arity, checked exceptions, domain clarity).

Java only provides up to two-argument built-ins (`BiFunction`, `BiPredicate`, etc.). For three or more arguments, you must define your own:

```java
@FunctionalInterface
public interface TriFunction<A, B, C, R> {
    R apply(A a, B b, C c);
}

// Usage
TriFunction<String, Integer, Boolean, String> format =
    (text, times, uppercase) -> {
        String repeated = text.repeat(times);
        return uppercase ? repeated.toUpperCase() : repeated;
    };

String result = format.apply("ha", 3, true); // "HAHAHA"
```

The `@FunctionalInterface` annotation:
- Documents intent clearly
- Causes a compile error if you accidentally add a second abstract method

### Custom interface with a checked exception

```java
@FunctionalInterface
public interface CheckedSupplier<T> {
    T get() throws Exception;
}
```

### Domain-specific example

```java
@FunctionalInterface
public interface PriceCalculator {
    BigDecimal calculate(Product product, int quantity);
}

// Usage
PriceCalculator bulkDiscount = (product, qty) ->
    product.unitPrice().multiply(BigDecimal.valueOf(qty * 0.9));
```

---

## Functional Interface vs. Static Method

This is the key difference in how Java uses functions as **values**.

### Static Method

```java
public class StringUtils {
    public static String normalize(String s) {
        return s.trim().toLowerCase();
    }
}

// Called directly — not a value, cannot be passed around
String result = StringUtils.normalize("  Hello  ");
```

A static method:
- Is called by name at a fixed call site
- Cannot be stored in a variable (without wrapping)
- Cannot be passed as an argument directly — only via a method reference
- Is resolved at compile time

### Functional Interface (function as a value)

```java
Function<String, String> normalize = s -> s.trim().toLowerCase();

// Stored in a variable
// Passed to another method
// Swapped out at runtime
List<String> results = inputs.stream()
    .map(normalize)
    .toList();
```

A functional interface instance:
- Is a **value** — can be stored, passed, returned
- Can be swapped at runtime (different strategies, configs)
- Enables higher-order functions (methods that accept behavior)
- Is the mechanism behind `stream().map(...)`, `filter(...)`, etc.

### Bridging both worlds: method references

A static method can be lifted into a functional interface using a method reference:

```java
Function<String, String> normalize = StringUtils::normalize;
//                                   ^^^^^^^^^^^^^^^^^^^^^^^^
//                        static method used as a Function value
```

This lets you write plain static methods and pass them as behavior wherever a functional interface is expected — best of both worlds.

```java
public class StringUtils {
    public static String normalize(String s) {
        return s.trim().toLowerCase();
    }
}

public static List<String> applyToAll(List<String> items, Function<String, String> fn) {
    return items.stream().map(fn).toList();
}
```

```java
// ✗ Does not compile — a method name is not a value
applyToAll(items, StringUtils.normalize);

// ✓ Works — method reference lifts it into a Function value
applyToAll(items, StringUtils::normalize);

// ✓ Works — lambda is already a value, passed directly
Function<String, String> normalize = s -> s.trim().toLowerCase();
applyToAll(items, normalize);
```

### Side-by-side comparison

| | Static Method | Functional Interface |
|---|---|---|
| Called by | Name at a fixed call site | `.apply()`, `.test()`, `.accept()`, etc. |
| Stored in variable | No | Yes |
| Passed as argument | Only via method reference | Yes, directly |
| Returned from method | Only via method reference | Yes, directly |
| Swappable at runtime | No | Yes |
| Checked exceptions | Allowed | Awkward — need custom interface |
| Performance | Direct call | Small lambda/closure overhead |
| Best for | Utility logic, pure transforms | Higher-order functions, strategy pattern |

### When to use each

Use a **static method** when:
- The behavior is fixed and always the same
- You just need to call it directly
- You want checked exceptions without wrapper ceremony

Use a **functional interface** when:
- You need to pass behavior into a method (e.g., `map`, `filter`, `sort`)
- You want to store or return behavior as a value
- You're implementing the strategy or decorator pattern
- You're working with the Stream API
