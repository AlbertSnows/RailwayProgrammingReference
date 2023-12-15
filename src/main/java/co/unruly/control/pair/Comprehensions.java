package co.unruly.control.pair;

import co.unruly.control.pair.Quad.QuadFunction;
import co.unruly.control.pair.Triple.TriFunction;
import co.unruly.control.result.Result;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static co.unruly.control.result.Transformers.attempt;
import static co.unruly.control.result.Transformers.onSuccess;

/**
 * Comprehensions seem to be a set of abstract functions that
 * define ways to apply various kinds of function to tuples
 */
@SuppressWarnings({"optional", "OptionalUsedAsFieldOrParameterType"})
public interface Comprehensions {

    /**
     * @param f function to apply pair to
     * @param <L> left
     * @param <R> right
     * @param <T> return type
     * @return function that applies bifunction to the provided pair
     */
    @Contract(pure = true)
    static <L, R, T> @NotNull Function<Pair<L, R>, T>
    onAll(BiFunction<L, R, T> f) {
        return pair -> pair.then(f);
    }

    /**
     * @param f the tri function to use
     * @param <A> .
     * @param <B> .
     * @param <C> .
     * @param <T> return type
     * @return a function that will apply f to the passed in triple
     */
    @Contract(pure = true)
    static <A, B, C, T> @NotNull Function<Triple<A, B, C>, T>
    onAll(TriFunction<A, B, C, T> f) {
        return triple -> triple.then(f);
    }

    /**
     * @param f the quad function to use
     * @param <A> .
     * @param <B> .
     * @param <C> .
     * @param <D> .
     * @param <T> return type
     * @return a function that takes a quad and applies the quad function to that quad
     */
    @Contract(pure = true)
    static <A, B, C, D, T> @NotNull Function<Quad<A, B, C, D>, T>
    onAll(QuadFunction<A, B, C, D, T> f) {
        return quad -> quad.then(f);
    }

    /**
     * @param maybeLeft left val
     * @param maybeRight right val
     * @param <L> type l
     * @param <R> type r
     * @return pair of left and right exists
     */
    static <L, R> Optional<Pair<L, R>>
    allOf(@NotNull Optional<L> maybeLeft, Optional<R> maybeRight) {
        return maybeLeft.flatMap(left ->
            maybeRight.map(right ->
                Pair.of(left, right)));
    }

    /**
     * @param maybeFirst .
     * @param maybeSecond .
     * @param maybeThird .
     * @param <A> .
     * @param <B> .
     * @param <C> .
     * @return tuple of values of three optionals
     */
    static <A, B, C> Optional<Triple<A, B, C>>
    allOf(@NotNull Optional<A> maybeFirst, Optional<B> maybeSecond, Optional<C> maybeThird) {
        return maybeFirst.flatMap(first ->
                maybeSecond.flatMap(second ->
                        maybeThird.map(third ->
                                Triple.of(first, second, third))));
    }

    /**
     * @param maybeFirst optional first
     * @param maybeSecond second
     * @param maybeThird third
     * @param maybeFourth fourth
     * @param <A> type a
     * @param <B> type b
     * @param <C> type c
     * @param <D> type d
     * @return quad tuple
     */
    static <A, B, C, D> Optional<Quad<A, B, C, D>>
    allOf(@NotNull Optional<A> maybeFirst, Optional<B> maybeSecond, Optional<C> maybeThird, Optional<D> maybeFourth) {
        return maybeFirst.flatMap(first ->
                maybeSecond.flatMap(second ->
                        maybeThird.flatMap(third ->
                                maybeFourth.map(fourth ->
                                        Quad.of(first, second, third, fourth)))));
    }

    /**
     * @param left left result
     * @param right right result
     * @param <F> failure
     * @param <LS> left side of pair
     * @param <RS> right side of pair
     * @return pair of left and right success
     */
    static <F, LS, RS> Result<Pair<LS, RS>, F>
    allOf(@NotNull Result<LS, F> left, Result<RS, F> right) {
        return left.then(attempt(l ->
            right.then(onSuccess(r ->
                Pair.of(l, r)))));
    }

    /**
     * @param first |
     * @param second |
     * @param third |
     * @param <F> |
     * @param <S1> |
     * @param <S2> |
     * @param <S3> |
     * @return tuple of three successes
     */
    static <F, S1, S2, S3> Result<Triple<S1, S2, S3>, F>
    allOf(@NotNull Result<S1, F> first, Result<S2, F> second, Result<S3, F> third) {
        return  first.then(attempt(firstValue ->
                    second.then(attempt(secondValue ->
                        third.then(onSuccess(thirdValue ->
                            Triple.of(firstValue, secondValue, thirdValue)))))));
    }

    /**
     * @param first .
     * @param second .
     * @param third .
     * @param fourth .
     * @param <F> .
     * @param <S1> .
     * @param <S2> .
     * @param <S3> .
     * @param <S4> .
     * @return collapses success states of four results to one
     */
    static <F, S1, S2, S3, S4> Result<Quad<S1, S2, S3, S4>, F>
    allOf(@NotNull Result<S1, F> first, Result<S2, F> second, Result<S3, F> third, Result<S4, F> fourth) {
        return  first.then(attempt(firstValue ->
                    second.then(attempt(secondValue ->
                        third.then(attempt(thirdValue ->
                            fourth.then(onSuccess(fourthValue ->
                                Quad.of(firstValue, secondValue, thirdValue, fourthValue)))))))));
    }


    /**
     * @param f test function
     * @param <F> failure type
     * @param <S1> left input
     * @param <S2> right input
     * @param <SR> success output
     * @return a function that when passed a result with a pair-fail setup, returns
     * applying that result to f
     */
    static <F, S1, S2, SR> @NotNull Function<Result<Pair<S1, S2>, F>, Result<SR, F>>
    ifAllSucceeded(BiFunction<S1, S2, SR> f) {
        return onSuccess(onAll(f));
    }

    /**
     * @param f tri function collapsor
     * @param <F> failure type
     * @param <S1> .
     * @param <S2> .
     * @param <S3> .
     * @param <SR> result type
     * @return dF(x -> y) where X is a result with success type triple and y is a result with a collapsed success type
     */
    static <F, S1, S2, S3, SR> @NotNull Function<Result<Triple<S1, S2, S3>, F>, Result<SR, F>>
    ifAllSucceeded(TriFunction<S1, S2, S3, SR> f) {
        return onSuccess(onAll(f));
    }

    /**
     * @param f quad function to collapse
     * @param <F> fail type
     * @param <S1> .
     * @param <S2> .
     * @param <S3> .
     * @param <S4> .
     * @param <SR> collapsed type
     * @return dF(x -> y) where x is a result with a success quad type and y is a result with a collapsed success type
     */
    static <F, S1, S2, S3, S4, SR> @NotNull Function<Result<Quad<S1, S2, S3, S4>, F>, Result<SR, F>>
    ifAllSucceeded(QuadFunction<S1, S2, S3, S4, SR> f) {
        return onSuccess(onAll(f));
    }

}
