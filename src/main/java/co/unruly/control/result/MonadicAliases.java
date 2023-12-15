package co.unruly.control.result;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static co.unruly.control.result.Introducers.success;
import static co.unruly.control.result.Transformers.*;

/**
 * Aliases for standard functions on Results which use names more familiar
 * to users of Haskell
 */
@SuppressWarnings("unused")
public interface MonadicAliases {

    /**
     * Returns a function which converts a regular value into a Result (as a Success)
     * @param <S> success
     * @param <F> failure
     * @return result type
     */
    @Contract(pure = true)
    static <S, F> @NotNull Function<S, Result<S, F>>
    pure() {
        return success();
    }

    /**
     * Returns a function which, when applied to a Result, applies the provided function to
     * the wrapped value if it's a Success, otherwise perpetuates the existing failure
     * @param f function to map success to
     * @param <Input> input type
     * @param <Output> success output type
     * @param <F> fail type
     * @return function that, given a result type, maps the success input
     * through the provided function and returns a new result with success output type
     */
    static <Input, Output, F> @NotNull Function<Result<Input, F>, Result<Output, F>>
    map(Function<Input, Output> f) {
        return onSuccess(f);
    }

    /**
     * Returns a function which, when applied to a Result, applies the provided function to
     * the wrapped value, returning that Result, if it's a Success. This can turn a Success into a
     * Failure.
     * <p>
     * If the result was already a failure, it perpetuates the existing failure.
     * @param f the mapping function
     * @param <Input> input type
     * @param <Output> output type
     * @param <F> failure type
     * @return a function that takes an input and maps it to a new output for success
     */
    @Contract(pure = true)
    static <Input, Output, F> @NotNull Function<Result<Input, F>, Result<Output, F>>
    flatMap(Function<Input, Result<Output, F>> f) {
        return attempt(f);
    }


    /**
     * Returns a function which, when applied to a Result, applies the provided function to
     * the wrapped value, returning that Result, if it's a Success. This can turn a Success into a
     * Failure.
     * <p>
     * If the result was already a failure, it perpetuates the existing failure.
     * @param f the mapping function
     * @param <Input> input type
     * @param <Output> output type
     * @param <F> fail type
     * @return a function that takes an input type and maps it to an output type
     * inside a result
     */
    @Contract(pure = true)
    static <Input, Output, F> @NotNull Function<Result<Input, F>, Result<Output, F>>
    bind(Function<Input, Result<Output, F>> f) {
        return attempt(f);
    }

    /**
     * Returns a function which, when applied to a Result, applies the provided function to
     * the wrapped value if it's a failure, otherwise perpetuates the existing success
     * @param f the mapping function
     * @param <S> success type
     * @param <Input> input type
     * @param <Output> output type
     * @return a function that maps a failure input to a new failure output
     */
    static <S, Input, Output> @NotNull Function<Result<S, Input>, Result<S, Output>>
    mapFailure(Function<Input, Output> f) {
        return onFailure(f);
    }

    /**
     * Returns a function which, when applied to a Result, applies the provided function to
     * the wrapped value, returning that Result, if it's a Failure. This can turn a Failure into a
     * Success.
     * <p>
     * If the result was already a success, it perpetuates the existing success.
     * @param f the mapping function
     * @param <S> success type
     * @param <Input> input type
     * @param <Output> output type
     * @return a function that maps a failure input to a new failure output
     */
    @Contract(pure = true)
    static <S, Input, Output> @NotNull Function<Result<S, Input>, Result<S, Output>>
    flatMapFailure(Function<Input, Result<S, Output>> f) {
        return recover(f);
    }


    /**
     * Returns a function which, when applied to a Result, applies the provided function to
     * the wrapped value, returning that Result, if it's a Failure. This can turn a Failure into a
     * Success.
     * <p>
     * If the result was already a success, it perpetuates the existing success.
     * @param f the mapping function
     * @param <S> success type
     * @param <Input> input type
     * @param <Output> output type
     * @return a function that maps a failure input to a new failure output
     */
    @Contract(pure = true)
    static <S, Input, Output> @NotNull Function<Result<S, Input>, Result<S, Output>>
    bindFailure(Function<Input, Result<S, Output>> f) {
        return recover(f);
    }
}
