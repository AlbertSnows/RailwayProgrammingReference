package co.unruly.control.result;

import co.unruly.control.pair.Pair;
import co.unruly.control.pair.Pairs;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;
import static java.util.function.Function.identity;
import static java.util.stream.Stream.empty;

/**
 * A set of common functions to convert from a <code>Result</code> to
 * an unwrapped value.
 */
@SuppressWarnings("unused")
public interface Resolvers {

    /**
     * Takes a Result where both success and failure types are the same, and returns
     * either the success or failure value as appropriate
     * @param <T> input type
     * @return T
     */
    @Contract(pure = true)
    static <T> @NotNull Function<Result<T, T>, T>
    collapse() {
        return r -> r.either(identity(), identity());
    }

    /**
     * Takes a Result and returns the success value if it is a success, or if it's
     * a failure, returns the result of applying the recovery function to the
     * failure value.
     * @param recoveryFunction function to recover from F
     * @param <OS> output success
     * @param <IS> input success
     * @param <FS> failure to recovery type
     * @param <F> failure type
     * @return output success or recovered failure
     */
    @Contract(pure = true)
    static <OS, IS extends OS, FS extends OS, F> @NotNull Function<Result<IS, F>, OS>
    ifFailed(Function<F, FS> recoveryFunction) {
        return r -> r.either(identity(), recoveryFunction);
    }

    /**
     * Takes a Result for which the failure type is an Exception, and returns the
     * success value if it's a success, or throws the failure exception, wrapped in a
     * RuntimeException.
     * @param <S> success type
     * @param <X> exception type
     * @return success value or throws an exception
     */
    @Contract(pure = true)
    static <S, X extends Exception> @NotNull Function<Result<S, X>, S>
    getOrThrow() {
        return r -> r.either(identity(), ex -> { throw new RuntimeException(ex); });
    }

    /**
     * Takes a Result and returns the success value if it is a success, or if it's
     * a failure, throws the result of applying the exception converter to the
     * failure value.
     * @param exceptionConverter converts F to an exception
     * @param <S> success type
     * @param <F> fail type
     * @return success or throws a runtime exception
     */
    @Contract(pure = true)
    static <S, F> @NotNull Function<Result<S, F>, S>
    getOrThrow(Function<F, RuntimeException> exceptionConverter) {
        return r -> r.either(identity(), failure -> { throw exceptionConverter.apply(failure); });
    }

    /**
     * Returns a Stream of successes: a stream of a single value if this is a success,
     * or an empty stream if this is a failure. This is intended to be used to flat-map
     * over a stream of Results to extract a stream of just the successes.
     * @param <S> success type
     * @param <F> success type
     * @return function that, given a result, provides a stream of successes
     * or an empty stream on failure
     */
    @Contract(pure = true)
    static <S, F> @NotNull Function<Result<S, F>, Stream<S>> successes() {
        return r -> r.either(Stream::of, __ -> empty());
    }

    /**
     * Returns a Stream of failures: a stream of a single value if this is a failure,
     * or an empty stream if this is a success. This is intended to be used to flat-map
     * over a stream of Results to extract a stream of just the failures.
     * @param <S> success type
     * @param <F> fail type
     * @return function that provides a stream of failures
     * or an empty stream on success, given a result
     */
    @Contract(pure = true)
    static <S, F> @NotNull Function<Result<S, F>, Stream<F>> failures() {
        return r -> r.either(__ -> empty(), Stream::of);
    }

    /**
     * Returns an Optional success value, which is present if this result was a failure
     * and empty if it was a failure.
     * @param <S> success type
     * @param <F> fail type
     * @return function that, given result, gives an optional if there was
     * a success, or None otherwise
     */
    @Contract(pure = true)
    static <S, F> @NotNull Function<Result<S, F>, Optional<S>>
    toOptional() {
        return r -> r.either(Optional::of, __ -> Optional.empty());
    }

    /**
     * Returns an Optional failure value, which is present if this result was a failure
     * and empty if it was a success.
     * @param <S> success type
     * @param <F> fail type
     * @return function that, given result, gives an optional if there was
     * a failure, or None otherwise
     */
    @Contract(pure = true)
    static <S, F> @NotNull Function<Result<S, F>, Optional<F>>
    toOptionalFailure() {
        return r -> r.either(__ -> Optional.empty(), Optional::of);
    }

    /**
     * Collects a Stream of Results into a Pair of Lists, the left containing the unwrapped
     * success values, the right containing the unwrapped failures.
     * @param <S> success type
     * @param <F> fail type
     * @return pair of lists with unwrapped successes and failures
     */
    @Contract(value = " -> new", pure = true)
    static <S, F> @NotNull Collector<Result<S, F>, Pair<List<S>, List<F>>, Pair<List<S>, List<F>>>
    split() {
        return new ResultCollector<>(pair -> Pair.of(unmodifiableList(pair.left()), unmodifiableList(pair.right())));
    }

    /**
     * Collects a Stream of Results into a Result which contains a List of Successes, if all results in
     * the stream were successful, or a list of Failures if any failed.
     * @param <S> success type
     * @param <F> fail type
     * @return collector with list of all successes or list of failures
     */
    @Contract(value = " -> new", pure = true)
    static <S, F> @NotNull Collector<Result<S, F>, Pair<List<S>, List<F>>, Result<List<S>, List<F>>>
    allSucceeded() {
        return new ResultCollector<>(Pairs::anyFailures);
    }

    /**
     * Collects a Stream of Results into a Result which contains a List of Successes, if any results in
     * the stream were successful, or a list of Failures if all failed.
     * @param <S> success type
     * @param <F> fail type
     * @return collector with list of successes, or list of all failures
     */
    @Contract(value = " -> new", pure = true)
    static <S, F> @NotNull Collector<Result<S, F>, Pair<List<S>, List<F>>, Result<List<S>, List<F>>>
    anySucceeded() {
        return new ResultCollector<>(Pairs::anySuccesses);
    }
}
