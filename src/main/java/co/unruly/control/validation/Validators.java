package co.unruly.control.validation;


import co.unruly.control.Optionals;
import co.unruly.control.ThrowingLambdas.ThrowingFunction;
import co.unruly.control.result.Result;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static co.unruly.control.result.Result.failure;
import static co.unruly.control.result.Result.success;
import static co.unruly.control.result.Transformers.onFailure;
import static co.unruly.control.result.Transformers.recover;

/**
 * interface for working with validation
 */
@SuppressWarnings("unused")
public interface Validators {

    /**
     * @param validators arbitrary set of validators
     * @param <T> input type
     * @param <E> error type
     * @return dF(x -> y) where dF represents the composition of the validators
     * and x is the input that is threaded through all of them
     */
    @Contract(pure = true)
    @SafeVarargs
    static <T, E> @NotNull Validator<T, E>
    compose(Validator<T, E>... validators) {
        return t -> Arrays.stream(validators).flatMap(v -> v.validate(t));
    }

    /**
     * @param test predicate function
     * @param error error type
     * @param <T> input type
     * @param <E> error type
     * @return dF(x -> y) where x yields error if passing test
     */
    static <T, E> @NotNull Validator<T, E>
    rejectIf(@NotNull Predicate<T> test, E error) {
        return acceptIf(test.negate(), error);
    }

    /**
     * @param test predicate function
     * @param errorGenerator f(T -> E)
     * @param <T> input type
     * @param <E> error type
     * @return dF(x -> y) where x yields output from errorGenerator
     * if passing test
     */
    static <T, E> @NotNull Validator<T, E>
    rejectIf(@NotNull Predicate<T> test, Function<T, E> errorGenerator) {
        return acceptIf(test.negate(), errorGenerator);
    }

    /**
     * @param test predicate function
     * @param error error type
     * @param <T> input type
     * @param <E> error type
     * @return dF(x -> y) where x yields error if failing test
     */
    @Contract(pure = true)
    static <T, E> @NotNull Validator<T, E>
    acceptIf(Predicate<T> test, E error) {
        return acceptIf(test, t -> error);
    }

    /**
     * @param test predicate function
     * @param errorGenerator generates errors from T
     * @param <T> input type
     * @param <E> error type
     * @return dF(x -> y) where if x fails test, it returns
     * a stream of E from errorGenerator
     */
    @Contract(pure = true)
    static <T, E> @NotNull Validator<T, E>
    acceptIf(Predicate<T> test, Function<T, E> errorGenerator) {
        return t -> test.test(t) ? Stream.empty() : Stream.of(errorGenerator.apply(t));
    }

    /**
     * @param validator validation function
     * @param <T> input type
     * @param <E> output type
     * @return optional stream getting the first result of the stream validation
     */
    @Contract(pure = true)
    static <T, E> @NotNull Validator<T, E> firstOf(Validator<T, E> validator) {
        return t -> Optionals.stream(validator.validate(t).findFirst());
    }

    /**
     * @param test predicate function
     * @param validator validation function
     * @param <T> input type
     * @param <E> error type
     * @return dF(x -> y) where x is tested and, if passed, is then validated
     * otherwise it returns an empty stream
     */
    @Contract(pure = true)
    static <T, E> @NotNull Validator<T, E> onlyIf(Predicate<T> test, Validator<T, E> validator) {
        return t -> test.test(t) ? validator.validate(t) : Stream.empty();
    }

    /**
     * @param validator maps input to validation outcome
     * @param errorMapper f((T, E) -> E1) maps input and error outcome to new error type
     * @param <T> input type
     * @param <E> input error type
     * @param <E1> output error type
     * @return dF(x -> y) where x is validated and mapped with any yielded errors into
     * the mapper
     */
    @Contract(pure = true)
    static <T, E, E1> @NotNull Validator<T, E1>
    mappingErrors(Validator<T, E> validator, BiFunction<T, E, E1> errorMapper) {
        return t -> validator.validate(t).map(e -> errorMapper.apply(t, e));
    }

    /**
     * @param accessor f(T -> T1)
     * @param innerValidator validator for T1
     * @param <T> input type
     * @param <T1> output type
     * @param <E> error type
     * @return dF(x -> y) where x converted to T1 and validated
     */
    @Contract(pure = true)
    static <T, T1, E> @NotNull Validator<T, E>
    on(Function<T, T1> accessor, Validator<T1, E> innerValidator) {
        return t -> innerValidator.validate(accessor.apply(t));
    }

    /**
     * @param accessor function that converts T -> T1 or throws
     * @param onException f(exception -> E), called when accessor throws
     * @param innerValidator validates T1 or yields E
     * @param <T> input type
     * @param <T1> output type
     * @param <E> error type
     * @param <X> exception type
     * @return dF(x -> y) where x is provided to accessor and then validated
     * y is the output of the validation wrapped in a try-catch where
     * the failure maps the exception to an error state
     */
    @Contract(pure = true)
    static <T, T1, E, X extends Exception> @NotNull Validator<T, E>
    tryOn(ThrowingFunction<T, T1, X> accessor, Function<Exception, E> onException, Validator<T1, E> innerValidator) {
        return t -> {
            try {
                return innerValidator.validate(accessor.apply(t));
            } catch (Exception e) {
                return Stream.of(onException.apply(e));
            }
        };
    }

    /**
     * @param iterator f(x1 -> Iterable(x2))
     * @param innerValidator f(x2 -> E)
     * @param <T> input type
     * @param <T1> output type
     * @param <E> error type
     * @return validator outcome
     */
    @Contract(pure = true)
    static <T, T1, E> @NotNull Validator<T, E>
    onEach(Function<T, Iterable<T1>> iterator, Validator<T1, E> innerValidator) {
        return t -> StreamSupport.stream(iterator.apply(t).spliterator(), false)
                .flatMap(innerValidator::validate);
    }

    /**
     * @param validatorWhichThrowsRuntimeExceptions validator function that throws
     * @param errorMapper f(exception -> error type)
     * @param <T> success type
     * @param <E> error type
     * @return dF(x -> y) where x is an input to our validator function
     * and y is the result of the validation or a stream of the error mapper
     * if an exception is thrown
     */
    @Contract(pure = true)
    static <T, E> @NotNull Validator<T, E>
    tryTo(Validator<T, E> validatorWhichThrowsRuntimeExceptions, Function<RuntimeException, E> errorMapper) {
        return t -> {
            try {
                return validatorWhichThrowsRuntimeExceptions.validate(t);
            } catch (RuntimeException ex) {
                return Stream.of(errorMapper.apply(ex));
            }
        };
    }

    /**
     * @param filterCondition predicate function to filter the failure validation
     * @param <T> success
     * @param <E> errors(?)
     * @return a function that expects a result, and does some filtering
     * based on the failure outcome
     */
    @Contract(pure = true)
    static <T, E> @NotNull Function<Result<T, FailedValidation<T, E>>, Result<T, FailedValidation<T, E>>>
    ignoreWhen(Predicate<E> filterCondition) {
        Function<FailedValidation<T, E>, FailedValidation<T, E>> filteredFailedValidation = fv -> {
            var t = fv.errors().stream()
                    .filter(filterCondition.negate())
                    .collect(Collectors.toList());
            return new FailedValidation<>(fv.value(), t);
        };
        Function<FailedValidation<T, E>, Result<T, FailedValidation<T, E>>> isEmptyResult =
                fv -> fv.errors().isEmpty() ? success(fv.value()) : failure(fv);
        return result -> result.then(onFailure(filteredFailedValidation)).then(recover(isEmptyResult));
    }

}
