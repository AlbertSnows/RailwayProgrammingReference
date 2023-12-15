package co.unruly.control.validation;

import co.unruly.control.ThrowingLambdas;
import co.unruly.control.matchers.ResultMatchers;
import co.unruly.control.pair.Pair;
import co.unruly.control.result.Result;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static co.unruly.control.matchers.ResultMatchers.isSuccessOf;
import static co.unruly.control.result.Resolvers.*;
import static co.unruly.control.result.Result.failure;
import static co.unruly.control.result.Transformers.onFailureDo;
import static co.unruly.control.result.Transformers.onSuccessDo;
import static co.unruly.control.validation.Validators.*;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class ValidatorTest {

    @Test
    public void canCreateValidatorsWithFixedErrorMessages() {
        Validator<Integer, String> isEven = acceptIf(divisibleBy(2), "odd");

        Result<Integer, FailedValidation<Integer, String>> validate4 = isEven.apply(4);
        Result<Integer, FailedValidation<Integer, String>> validate5 = isEven.apply(5);

        assertTrue(isSuccessOf(4).matches(validate4));

        assertTrue(isFailedValidationOf(5, "odd").matches(validate5));
    }


    @Test
    public void canCreateValidatorsWithDynamicErrorMessages() {
        Validator<Integer, String> isEven =
                acceptIf(divisibleBy(2), x -> String.format("%d is odd", x));

        Result<Integer, FailedValidation<Integer, String>> validate4 = isEven.apply(4);
        Result<Integer, FailedValidation<Integer, String>> validate5 = isEven.apply(5);

        assertTrue(isSuccessOf(4).matches(validate4));

        assertTrue(isFailedValidationOf(5, "5 is odd").matches(validate5));
    }

    @Test
    public void canComposeValidators() {
        Validator<Integer, String> fizzbuzz = compose(
                rejectIf(divisibleBy(3), "fizz"),
                rejectIf(divisibleBy(5), x -> String.format("%d is a buzz", x)));

        Result<Integer, FailedValidation<Integer, String>> validate4 = fizzbuzz.apply(4);
        Result<Integer, FailedValidation<Integer, String>> validate5 = fizzbuzz.apply(15);

        assertTrue(isSuccessOf(4).matches(validate4));

        assertTrue(isFailedValidationOf(15, "fizz", "15 is a buzz").matches(validate5));
    }


    @Test
    public void canComposeValidatorsForFirstError() {
        Validator<Integer, String> fizzbuzz = firstOf(compose(
                rejectIf(divisibleBy(3), "fizz"),
                rejectIf(divisibleBy(5), x -> String.format("%d is a buzz", x))));

        Result<Integer, FailedValidation<Integer, String>> validate5 = fizzbuzz.apply(5);
        Result<Integer, FailedValidation<Integer, String>> validate15 = fizzbuzz.apply(15);

        assertTrue(isFailedValidationOf(5, "5 is a buzz").matches(validate5));
        assertTrue(isFailedValidationOf(15, "fizz").matches(validate15));
    }

    @Test
    public void doesNotExecuteValidatorsIfAlreadyFailedAndOnlyReportingFirst() {
        Validator<Integer, String> fizzbuzz = firstOf(compose(
                rejectIf(divisibleBy(3), "fizz"),
                rejectIf(divisibleBy(5), x -> {
                    throw new AssertionError("should not exercise this method"); })));

        Validator<Integer, String> biglittle = firstOf(compose(
                rejectIf(x -> x > 10, "big"),
                rejectIf(x -> x < 3, x -> {
                    throw new AssertionError("should not exercise this method"); })));

        Validator<Integer, String> combined = compose(fizzbuzz, biglittle);

        Result<Integer, FailedValidation<Integer, String>> validate15 = combined.apply(15);

        assertTrue(isFailedValidationOf(15, "fizz", "big").matches(validate15));
    }

    @Test
    public void canStreamSuccesses() {
        Validator<Integer, String> isEven = acceptIf(divisibleBy(2), "odd");

        List<Integer> evens = Stream.of(1,2,3,4,5,6,7,8,9)
                .map(isEven)
                .flatMap(successes())
                .collect(toList());

        assertTrue(hasItems(2,4,6,8).matches(evens));
    }

    @Test
    public void canStreamFailures() {
        Validator<Integer, String> isEven = acceptIf(divisibleBy(2), "odd");

        List<FailedValidation<Integer, String>> odds =
                Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                .map(isEven)
                .flatMap(failures())
                .collect(toList());

        assertTrue(hasItems(
                validationFailure(1, "odd"),
                validationFailure(3, "odd"),
                validationFailure(5, "odd"),
                validationFailure(7, "odd"),
                validationFailure(9, "odd")).matches(odds));
    }

    @Test
    public void canConsumeSuccesses() {
        Consumer<Integer> log = mock(Consumer.class);

        Validator<Integer, String> isPrime = compose(
                rejectIf(multipleOf(2), x -> x + " divides by 2"),
                rejectIf(multipleOf(3), x -> x + " divides by 3"),
                rejectIf(multipleOf(5), x -> x + " divides by 5"),
                rejectIf(multipleOf(7), x -> x + " divides by 7")
        );

        Stream.of(1,2,3,4,5,6,7,8,9).map(isPrime).forEach(onSuccessDo(log));

        verify(log).accept(1);
        verify(log).accept(2);
        verify(log).accept(3);
        verify(log).accept(5);
        verify(log).accept(7);
        verifyNoMoreInteractions(log);
    }

    @Test
    public void canConsumeFailures() {
        Consumer<FailedValidation<Integer, String>> log = mock(Consumer.class);

        Validator<Integer, String> isPrime = compose(
                rejectIf(multipleOf(2), x -> x + " divides by 2"),
                rejectIf(multipleOf(3), x -> x + " divides by 3"),
                rejectIf(multipleOf(5), x -> x + " divides by 5"),
                rejectIf(multipleOf(7), x -> x + " divides by 7")
        );

        Stream.of(1,2,3,4,5,6,7,8,9).map(isPrime).forEach(onFailureDo(log));

        verify(log).accept(validationFailure(4, "4 divides by 2"));
        verify(log).accept(validationFailure(6, "6 divides by 2", "6 divides by 3"));
        verify(log).accept(validationFailure(8, "8 divides by 2"));
        verify(log).accept(validationFailure(9, "9 divides by 3"));
        verifyNoMoreInteractions(log);
    }

    @Test
    public void canFireFirstErrorForEachFailure() {
        Consumer<FailedValidation<Integer, String>> log = mock(Consumer.class);

        Validator<Integer, String> isPrime = firstOf(compose(
                rejectIf(multipleOf(2), x -> x + " divides by 2"),
                rejectIf(multipleOf(3), x -> x + " divides by 3"),
                rejectIf(multipleOf(5), x -> x + " divides by 5"),
                rejectIf(multipleOf(7), x -> x + " divides by 7")
        ));

        Stream.of(1,2,3,4,5,6,7,8,9).map(isPrime).forEach(onFailureDo(log));

        verify(log).accept(validationFailure(4, "4 divides by 2"));
        verify(log).accept(validationFailure(6, "6 divides by 2"));
        verify(log).accept(validationFailure(8, "8 divides by 2"));
        verify(log).accept(validationFailure(9, "9 divides by 3"));
        verifyNoMoreInteractions(log);
    }

    @Test
    public void canCreateConditionalValidator() {
        Validator<List<Integer>, String> containsEvens = acceptIf(
                list -> list.stream().noneMatch(x -> x % 2 == 0),
                "List contains even numbers");

        Validator<List<Integer>, String> onlyChecksEvenLengthLists = onlyIf(
                list -> list.size() % 2 == 0,
                containsEvens
        );

        Result<List<Integer>, FailedValidation<List<Integer>, String>> ofFiveNumbers
                = onlyChecksEvenLengthLists.apply(asList(1, 2, 3, 4, 5));
        Result<List<Integer>, FailedValidation<List<Integer>, String>> ofSixNumbers
                = onlyChecksEvenLengthLists.apply(asList(1, 2, 3, 4, 5, 6));

        assertTrue(isSuccessOf(asList(1,2,3,4,5)).matches(ofFiveNumbers));
        assertTrue(isFailedValidationOf(asList(1,2,3,4,5,6), "List contains even numbers")
                .matches(ofSixNumbers));
    }

    @Test
    public void canFireAllErrorsForEachFailure() {
        BiConsumer<Integer, String> log = mock(BiConsumer.class);

        Validator<Integer, String> isPrime = compose(
                rejectIf(multipleOf(2), x -> x + " divides by 2"),
                rejectIf(multipleOf(3), x -> x + " divides by 3"),
                rejectIf(multipleOf(5), x -> x + " divides by 5"),
                rejectIf(multipleOf(7), x -> x + " divides by 7")
        );

        Stream.of(1,2,3,4,5,6,7,8,9).map(isPrime)
                .forEach(onFailureDo(v -> v.errors().forEach(e -> log.accept(v.value(), e))));

        verify(log).accept(4, "4 divides by 2");
        verify(log).accept(6, "6 divides by 2");
        verify(log).accept(6, "6 divides by 3");
        verify(log).accept(8, "8 divides by 2");
        verify(log).accept(9, "9 divides by 3");
        verifyNoMoreInteractions(log);
    }

    @Test
    public void canMapErrors() {
        BiConsumer<Integer, String> log = mock(BiConsumer.class);

        Validator<Integer, String> isPrime = mappingErrors(compose(
                rejectIf(multipleOf(2), x -> x + " divides by 2"),
                rejectIf(multipleOf(3), x -> x + " divides by 3"),
                rejectIf(multipleOf(5), x -> x + " divides by 5"),
                rejectIf(multipleOf(7), x -> x + " divides by 7")
        ), (num, msg) -> msg + ", oh boy");

        Stream.of(1,2,3,4,5,6,7,8,9).map(isPrime)
                .forEach(onFailureDo(v -> v.errors().forEach(e -> log.accept(v.value(), e))));

        verify(log).accept(4, "4 divides by 2, oh boy");
        verify(log).accept(6, "6 divides by 2, oh boy");
        verify(log).accept(6, "6 divides by 3, oh boy");
        verify(log).accept(8, "8 divides by 2, oh boy");
        verify(log).accept(9, "9 divides by 3, oh boy");
        verifyNoMoreInteractions(log);
    }

    @Test
    public void canSplitResults() {
        Validator<Integer, String> isPrime = compose(
                rejectIf(multipleOf(2), x -> x + " divides by 2"),
                rejectIf(multipleOf(3), x -> x + " divides by 3"),
                rejectIf(multipleOf(5), x -> x + " divides by 5"),
                rejectIf(multipleOf(7), x -> x + " divides by 7")
        );

        Pair<List<Integer>, List<FailedValidation<Integer, String>>> results = Stream
                .of(4,5,6,7,8)
                .map(isPrime)
                .collect(split());

        assertTrue(hasItems(5, 7).matches(results.left()));
        assertTrue(hasItems(
                validationFailure(4, "4 divides by 2"),
                validationFailure(6, "6 divides by 2", "6 divides by 3"),
                validationFailure(8, "8 divides by 2")
                ).matches(results.right()));
    }

    @Test
    public void canIgnoreErrors() {
        Result<Integer, FailedValidation<Integer, String>> failedValidation
                = failure(new FailedValidation<>(42, asList("fail1", "fail2", "error1")));

        Result<Integer, FailedValidation<Integer, String>> filteredValidation = failedValidation
                .then(ignoreWhen(error -> error.startsWith("fail")));

        assertTrue(isFailedValidationOf(42, "error1").matches(filteredValidation));
    }

    @Test
    public void convertsToSuccessWhenAllErrorsIgnored() {
        Result<Integer, FailedValidation<Integer, String>> failedValidation
                = failure(new FailedValidation<>(42, asList("fail1", "fail2", "fail3")));

        Result<Integer, FailedValidation<Integer, String>> filteredValidation = failedValidation
                .then(ignoreWhen(error -> error.startsWith("fail")));

        assertTrue(isSuccessOf(42).matches(filteredValidation));
    }

    @Test
    public void blammo() {
        safelyDoSomethingDodgy(x -> { throw new Exception("hello"); });
    }

    private static void safelyDoSomethingDodgy(ThrowingLambdas.ThrowingConsumer<String, Exception> consumer) {
        try {
            consumer.accept("cheese");
        } catch (Exception ex) {
            // do nothing cos that's how I roll

        }
    }

    @Contract(pure = true)
    private static @NotNull Predicate<Integer> divisibleBy(int factor) {
        return x -> x % factor == 0;
    }

    @Contract(pure = true)
    private static @NotNull Predicate<Integer> multipleOf(int factor) {
        return x -> x != factor && x % factor == 0;
    }

    @Contract("_, _ -> new")
    @SafeVarargs
    private <T, E> @NotNull FailedValidation<T, E> validationFailure(T value, E... errors) {
        return new FailedValidation<>(value, asList(errors));
    }

    private <T, E> @NotNull Matcher<Result<T, FailedValidation<T, E>>>
    isFailedValidationOf(T value, E... errors) {
        FailedValidation<T, E> failedValidation = validationFailure(value, errors);
        return ResultMatchers.isFailureOf(failedValidation);
    }
}
