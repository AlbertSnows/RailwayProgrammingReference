package co.unruly.control.result;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A collection of functions to (conditionally) recover a failure into a success.
 */
@SuppressWarnings("unused")
public interface Recover {

    /**
     * Returns a function which takes an Optional value, and returns a failure of the
     * wrapped value if it was present, otherwise returns a success using the provided Supplier
     * @param onEmpty supplier function
     * @param <S> success type
     * @param <F> fail type
     * @return dF(x -> y) where x is an optional such that if x is a
     * failure it gets wrapped, otherwise the supplier is called
     */
    @Contract(pure = true)
    static <S, F> @NotNull Function<Optional<F>, Result<S, F>>
    whenAbsent(Supplier<S> onEmpty) {
        return maybe -> maybe.map(Result::<S, F>failure).orElseGet(() -> Result.success(onEmpty.get()));
    }

    /**
     * Takes a class and a mapping function and returns a function which takes a value and, if it's of the
     * provided class, applies the mapping function to it and returns it as a Success, otherwise returning
     * the input value as a Failure.
     * @param targetClass to map to
     * @param mapper maps from NarrowFailType to S
     * @param <S> success type
     * @param <BroadFailType> broad fail type
     * @param <NarrowFailType> narrow fail type
     * @return dF(BroadFailType -> R(S, BroadFailType)) where
     * NarrowFailType !extends BroadFailType -> R(BroadFailType)
     * NarrowFailType extends BroadFailType -> mapper(BroadFailType as NarrowFailType) -> R(S)
     */
    static <S, BroadFailType, NarrowFailType extends BroadFailType> @NotNull
            Function<BroadFailType, Result<S, BroadFailType>>
    ifType(Class<NarrowFailType> targetClass, Function<NarrowFailType, S> mapper) {
        return Introducers.<BroadFailType, NarrowFailType>castTo(targetClass)
                .andThen(Transformers.onSuccess(mapper));
    }

    /**
     * Takes a predicate and a mapping function and returns a function which takes a value and, if it satisfies
     * the predicate, applies the mapping function to it and returns it as a Success, otherwise returning
     * the input value as a Failure.
     * @param test predicate function
     * @param mapper mapping function
     * @param <S> success type
     * @param <F> fail type
     * @return dF(x -> y) where if x satisfies test, return mapper(x) as a success or
     * failure otherwise
     */
    @Contract(pure = true)
    static <S, F> @NotNull Function<F, Result<S, F>>
    ifIs(Predicate<F> test, Function<F, S> mapper) {
        return input -> test.test(input)
            ? Result.success(mapper.apply(input))
            : Result.failure(input);
    }

    /**
     * Takes a predicate and a mapping function and returns a function which takes a value and, if it doesn't
     * satisfy the predicate, applies the mapping function to it and returns it as a Success, otherwise returning
     * the input value as a Failure.
     * @param test testing function
     * @param mapper mapping function
     * @param <S> success type
     * @param <F> fail type
     * @return dF(x -> y) where if x doesn't satisfy test, x is passed to mapper and put into a success
     * and is otherwise a failure
     */
    static <S, F> @NotNull Function<F, Result<S, F>>
    ifNot(@NotNull Predicate<F> test, Function<F, S> mapper) {
        return ifIs(test.negate(), mapper);
    }

    /**
     * Takes a value and a mapping function and returns a function which takes a value and, if it is equal to
     * the provided value, applies the mapping function to it and returns it as a Success, otherwise returning
     * the input value as a Failure.
     * @param expectedValue value to check against input
     * @param mapper mapping function for success case
     * @param <S> success type
     * @param <F> fail type
     * @return dF(x -> y) where y is a result whose success applies the mapper to x
     */
    @Contract(pure = true)
    static <S, F> @NotNull Function<F, Result<S, F>>
    ifEquals(F expectedValue, Function<F, S> mapper) {
        return input -> expectedValue.equals(input)
            ? Result.success(mapper.apply(input))
            : Result.failure(input);
    }

    /**
     * Matches the value if the provided function yields an Optional whose value is
     * present, returning the value in that Optional.
     * @param successProvider function providing an optional success
     * @param <S> success type
     * @param <F> fail type
     * @return function that, given an input, pipes it through success provider into a result
     */
    @Contract(pure = true)
    static <S, F> @NotNull Function<F, Result<S, F>>
    ifPresent(Function<F, Optional<S>> successProvider) {
        return value -> successProvider
                .apply(value)
                .map(Result::<S, F>success)
                .orElseGet(() -> Result.failure(value));
    }
}
