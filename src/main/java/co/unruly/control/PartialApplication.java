package co.unruly.control;

import co.unruly.control.pair.Triple.TriFunction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A collection of functions to partially apply arguments to functions, to simplify usage
 * in streams, optionals etc.
 */
@SuppressWarnings("unused")
public interface PartialApplication {

    /**
     * Binds the provided argument to the function, and returns a Supplier with that argument applied.
     * <p>
     * bind(f, a) is equivalent to () -> f.apply(a)
     * @param f binding function
     * @param input to bind to f
     * @param <I> input type
     * @param <O> output type
     * @return supplier of O
     */
    @Contract(pure = true)
    static <I, O> @NotNull Supplier<O>
    bind(Function<I, O> f, I input) {
        return () -> f.apply(input);
    }

    /**
     * Binds the provided argument to the function, and returns a new Function with that argument already applied.
     * <p>
     * bind(f, a) is equivalent to b -> f.apply(a, b)
     * @param f binding function
     * @param firstParam to bind
     * @param <A> left type
     * @param <B> right type
     * @param <R> result type
     * @return function that asks for B, and gives R
     */
    @Contract(pure = true)
    static <A, B, R> @NotNull Function<B, R>
    bind(BiFunction<A, B, R> f, A firstParam) {
        return secondParam -> f.apply(firstParam, secondParam);
    }

    /**
     * Binds the provided arguments to the function, and returns a new Supplier with those arguments already applied.
     * <p>
     * bind(f, a, b) is equivalent to () -> f.apply(a, b)
     * @param f binding function
     * @param firstParam to bind
     * @param secondParam to bind
     * @param <A> left type
     * @param <B> right type
     * @param <R> result type
     * @return supplier of R
     */
    @Contract(pure = true)
    static <A, B, R> @NotNull Supplier<R>
    bind(BiFunction<A, B, R> f, A firstParam, B secondParam) {
        return () -> f.apply(firstParam, secondParam);
    }

    /**
     * Binds the provided argument to the function, and returns a new BiFunction with that argument already applied.
     * <p>
     * bind(f, a) is equivalent to (b, c) -> f.apply(a, b, c)
     * @param f function to bind
     * @param firstParam to bind
     * @param <A> firstParam type
     * @param <B> left type of closure
     * @param <C> right type of closure
     * @param <R> result type
     * @return bifunction that, when executed, provides R
     */
    @Contract(pure = true)
    static <A, B, C, R> @NotNull BiFunction<B, C, R>
    bind(TriFunction<A, B, C, R> f, A firstParam) {
        return (secondParam, thirdParam) -> f.apply(firstParam, secondParam, thirdParam);
    }

    /**
     * Binds the provided arguments to the function, and returns a new Function with those arguments already applied.
     * <p>
     * bind(f, a, b) is equivalent to c -> f.apply(a, b, c)
     * @param f binding function
     * @param firstParam to bind
     * @param secondParam to bind
     * @param <A> firstParam type
     * @param <B> secondParam type
     * @param <C> final type argument
     * @param <R> result type
     * @return function that asks for third argument, returns R
     */
    @Contract(pure = true)
    static <A, B, C, R> @NotNull Function<C, R>
    bind(TriFunction<A, B, C, R> f, A firstParam, B secondParam) {
        return thirdParam -> f.apply(firstParam, secondParam, thirdParam);
    }
}
