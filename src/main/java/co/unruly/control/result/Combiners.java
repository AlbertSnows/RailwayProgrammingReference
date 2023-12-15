package co.unruly.control.result;

import java.util.function.BiFunction;
import java.util.function.Function;

import static co.unruly.control.result.Result.success;

/**
 * interface for combining different data structures
 */
public interface Combiners {

    /**
     * Combines two Results into a single Result. If both arguments are a Success, then
     * it applies the given function to their values and returns a Success of it.
     * @param <A> a
     * @param <B> b
     * @param <F> f
     * @param secondArgument result to merge
     * @return first failure
     * If either or both arguments are Failures, then this returns the first failure
     * it encountered.
     */
    @org.jetbrains.annotations.NotNull
    @org.jetbrains.annotations.Contract(pure = true)
    static <A, B, F> Function<Result<A, F>, MergeableResults<A, B, F>>
    combineWith(Result<B, F> secondArgument) {
        // ugh ugh ugh we need an abstract class because otherwise it can't infer generics properly can i be sick now? ta
        return result -> new MergeableResults<>() {
            /**
             * @param combiner combining mechanism
             * @param <C>      combiner
             * @return an either
             */
            @Override
            public <C> Result<C, F> using(BiFunction<A, B, C> combiner) {
                return result.either(
                        s1 -> secondArgument.either(
                                s2 -> success(combiner.apply(s1, s2)),
                                Result::failure
                        ),
                        Result::failure
                );
            }
        };
    }

    /**
     * @param <A> input 1
     * @param <B> input 2
     * @param <F> failure
     */
    @FunctionalInterface
    interface MergeableResults<A, B, F>  {
        /**
         * @param combiner combining function
         * @param <C> success combiner
         * @return result with the combined result as the success and fail as fail
         */
        <C> Result<C, F> using(BiFunction<A, B, C> combiner);
    }
}
