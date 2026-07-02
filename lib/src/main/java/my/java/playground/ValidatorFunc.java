package my.java.playground;

import java.util.Optional;

@FunctionalInterface
public interface ValidatorFunc<T> {
  Optional<T> apply(T t);
}
