package co.unruly.control.pair;

import co.unruly.control.result.Result;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static co.unruly.control.result.Result.failure;
import static co.unruly.control.result.Result.success;
import static java.util.stream.Collectors.toList;

/**
 * Convenience functions on Pairs
 */
public interface Pairs {

    /**
     * Applies the given function to the left element of a Pair, returning a new Pair with the result of that
     * function as the left element and the original right element untouched
     */
    static <OL, NL, R> Function<Pair<OL, R>, Pair<NL, R>> onLeft(Function<? super OL, NL> leftMapper) {
        return pair -> Pair.of(leftMapper.apply(pair.left), pair.right);
    }

    /**
     * Applies the given function to the right element of a Pair, returning a new Pair with the result of that
     * function as the right element and the original left element untouched
     */
    static <L, OR, NR> Function<Pair<L, OR>, Pair<L, NR>> onRight(Function<OR, NR> rightMapper) {
        return pair -> Pair.of(pair.left, rightMapper.apply(pair.right));
    }

    /**
     * Applies the given function to both elements off a Pair, assuming that both elements are of the
     * same type
     */
    static <T, R> Function<Pair<T, T>, Pair<R, R>> onBoth(Function<T, R> f) {
        return pair -> Pair.of(f.apply(pair.left), f.apply(pair.right));
    }

    /**
     * Applies the given function to both elements off a Pair, yielding a non-Pair value
     */
    static <L, R, T> Function<Pair<L, R>, T> merge(BiFunction<L, R, T> f) {
        return pair -> pair.then(f);
    }

    /**
     * Merges a Pair of Lists of T into a single List of T, with the left items at the front of the list.
     */
    static <T> Function<Pair<List<T>, List<T>>, List<T>> mergeLists() {
        return pair -> Stream.of(pair.left, pair.right).flatMap(List::stream).collect(toList());
    }

    /**
     * Collects a Stream of Pairs into a single Pair of lists, where a given index can be used to access the left
     * and right parts of the input pairs respectively.
     */
    static <L, R> Collector<Pair<L, R>, Pair<List<L>, List<R>>, Pair<List<L>, List<R>>> toParallelLists() {
        return using(Collections::unmodifiableList, Collections::unmodifiableList);
    }

    /**
     * Collects a Stream of Pairs into a single Pair of arrays, where a given index can be used to access the left
     * and right parts of the input pairs respectively.
     */
    static <L, R> Collector<Pair<L, R>, Pair<List<L>, List<R>>, Pair<L[], R[]>> toArrays(IntFunction<L[]> leftArrayConstructor, IntFunction<R[]> rightArrayConstructor) {
        return using(
                left -> left.stream().toArray(leftArrayConstructor),
                right -> right.stream().toArray(rightArrayConstructor)
        );
    }

    /**
     * Reduces a stream of pairs to a single pair, using the provided identities and reducer functions
     */
    static <L, R> PairReducingCollector<L, R> reducing(
            L leftIdentity, BinaryOperator<L> leftReducer,
            R rightIdentity, BinaryOperator<R> rightReducer) {
        return new PairReducingCollector<>(leftIdentity, rightIdentity, leftReducer, rightReducer);
    }


    static <L, R, FL, FR> Collector<Pair<L, R>, Pair<List<L>, List<R>>, Pair<FL, FR>> using(
            Function<List<L>, FL> leftFinisher,
            Function<List<R>, FR> rightFinisher) {
        return new PairListCollector<>(leftFinisher, rightFinisher);
    }

    /**
     * If there are any elements in the right side of the Pair, return a failure of
     * the right side, otherwise return a success of the left.
     */
    static <L, R> Result<List<L>, List<R>> anyFailures(Pair<List<L>, List<R>> sides) {
        return sides.right.isEmpty() ? success(sides.left) : failure(sides.right);
    }

    /**
     * If there are any elements in the left side of the Pair, return a success of
     * the left side, otherwise return a failure of the left.
     */
    static <L, R> Result<List<L>, List<R>> anySuccesses(Pair<List<L>, List<R>> sides) {
        return sides.left.isEmpty() ? failure(sides.right) : success(sides.left);
    }
}
