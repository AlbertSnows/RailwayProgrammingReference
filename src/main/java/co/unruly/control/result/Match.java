package co.unruly.control.result;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.stream.Stream;

import static co.unruly.control.Piper.pipe;
import static co.unruly.control.HigherOrderFunctions.compose;
import static co.unruly.control.result.Resolvers.ifFailed;

/**
 * A small DSL for building compact dispatch tables: better than if-expressions, worse than
 * proper pattern matching. But hey, it's Java, what do you expect?
 * <p>
 * This models a match attempt as a sequence of operations on a Result, starting with a Failure
 * and continuously trying to use flatMapFailure to convert that Result into a Success.
 */
public class Match {

    /**
     * Builds a dispatch function from the provided matchers. Note that in order to yield
     * a function, the otherwise() method must be called on the result of this function:
     * as there's no way to determine if the dispatch table is complete, a base case is
     * required.
     * @param potentialMatchers an arbitrary list of matching functions
     * @param <I> input type
     * @param <O> success output type
     * @return a match attempt type representing the outcome when given an input
     */
    @Contract(pure = true)
    @SafeVarargs
    public static <I, O> @NotNull MatchAttempt<I, O>
    match(Function<I, Result<O, I>>... potentialMatchers) {
        return f -> attemptMatch(potentialMatchers).andThen(ifFailed(f));
    }

    /**
     * Builds a dispatch function from the provided matchers. Note that this returns a Result,
     * as there's no way to determine if the dispatch table is complete: if no match is found,
     * returns a Failure of the input value.
     * @param potentialMatchers an arbitrary number of matching functions
     * @param <I> input type
     * @param <O> success output type
     * @return a matching function that maps an input I to a result output
     */
    @SafeVarargs
    public static <I, O> @NotNull Function<I, Result<O, I>>
    attemptMatch(Function<I, Result<O, I>>... potentialMatchers) {
        return compose(Stream.of(potentialMatchers).map(Transformers::recover)).compose(Result::failure);
    }

    /**
     * Dispatches a value across the provided matchers. Note that in order to yield
     * a value, the otherwise() method must be called on the result of this function:
     * as there's no way to determine if the dispatch table is complete, a base case is
     * required.
     * @param inputValue starting value
     * @param potentialMatchers  arbitrary number of matching functions
     * @param <I> input type
     * @param <O> output type
     * @return match attempt result based on passed in failure value
     */
    @Contract(pure = true)
    @SafeVarargs
    public static <I, O> @NotNull BoundMatchAttempt<I, O>
    matchValue(I inputValue, Function<I, Result<O, I>>... potentialMatchers) {
        return f -> pipe(inputValue)
                .then(attemptMatch(potentialMatchers))
                .then(ifFailed(f))
                .resolve();
    }

    /**
     * @param <I> input
     * @param <O> output
     */
    @FunctionalInterface
    public interface MatchAttempt<I, O> {
        /**
         * @param baseCase .
         * @return basecase
         */
        Function<I, O> otherwise(Function<I, O> baseCase);
    }

    /**
     * @param <I> input
     * @param <O> output
     */
    @FunctionalInterface
    public interface BoundMatchAttempt<I, O> {
        /**
         * @param baseCase .
         * @return output
         */
        O otherwise(Function<I, O> baseCase);
    }
}
