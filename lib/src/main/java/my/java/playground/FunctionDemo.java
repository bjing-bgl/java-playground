package my.java.playground;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class FunctionDemo {
  // compose simple functions
  static Function<String, String> transformer1 = String::toUpperCase;
  static Function<String, String> transformer2 = String::toLowerCase;

  public static List<Function<String, String>> transformers = List.of(
      transformer1,
      transformer2
  );

  String input = "Hello World!";

  sealed interface Either<L, R> permits Either.Left, Either.Right {
    record Left<L, R>(L value) implements Either<L, R> {}
    record Right<L, R>(R value) implements Either<L, R> {}
  }

  // TODO compose more complex functions
  static ValidatorFunc<String> validator1 = x -> x.isBlank() ? Optional.empty(): Optional.of(x);
  // no white spaces
  static ValidatorFunc<String> validator2 = x -> x.chars().noneMatch(Character::isWhitespace) ? Optional.of(x) : Optional.empty();
  // trimmed
  static ValidatorFunc<String> validator3 = x -> x.equals(x.strip()) ? Optional.of(x) : Optional.empty();
  // no uppercase
  static ValidatorFunc<String> validator4 = x -> x.equals(x.toLowerCase()) ? Optional.of(x) : Optional.empty();

  static List<ValidatorFunc<String>> validators = List.of(
      validator1,
      validator2,
      validator3,
      validator4
  );

  public static ValidatorFunc<String> kleisli(
      ValidatorFunc<String> f,
      ValidatorFunc<String> g) {
    return x -> f.apply(x).flatMap(g::apply);
  }
}
