package my.java.playground;

import java.util.Optional;
import java.util.function.Function;
import org.junit.Test;

public class FunctionDemoTest {
  @Test
  public void testFunction() {

    String input = "Hello World!";

    String result1 = FunctionDemo.transformer1.apply(input);
    String result2 = FunctionDemo.transformer2.apply(result1);

    System.out.println(result2);
  }

  @Test
  public void testFunction2() {
    String input = "Hello World!";

    Function<String, String> composedTransformers = FunctionDemo.transformers
        .stream()
        .reduce(
          Function.identity(),
          Function::andThen
    );

    String result = composedTransformers.apply(input);
    System.out.println(result);
  }

  @Test
  public void testMoreComplexFunction3() {
    String input = "Hello World!";
    Optional<String> result = FunctionDemo.validator1.apply(input);

    // TODO talk about using result 1 in both validator 2 and 3
    Optional.of(input)
        .flatMap(s -> FunctionDemo.validator1.apply(s))
        .flatMap(s -> FunctionDemo.validator2.apply(s))
        .flatMap(s -> FunctionDemo.validator3.apply(s));

    ValidatorFunc<String> composedValidator = FunctionDemo.validators
        .stream()
        .reduce(
            Optional::of,
            FunctionDemo::kleisli
        );

    Optional<String> finalResult = composedValidator.apply(input);
  }
}
