package co.unruly.control.pair;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

/**
 * @param first .
 * @param second .
 * @param third .
 * @param <A> .
 * @param <B> .
 * @param <C> .
 */
@SuppressWarnings("unused")
public record Triple<A, B, C>(A first, B second, C third) {

    /**
     * @param <A> .
     * @param <B> .
     * @param <C> .
     * @param <R> result type
     */
    @FunctionalInterface
    public interface TriFunction<A, B, C, R> {
        /**
         * @param a .
         * @param b .
         * @param c .
         * @return R
         */
        R apply(A a, B b, C c);
    }

    /**
     * @param first .
     * @param second .
     * @param third .
     * @param <A> .
     * @param <B> .
     * @param <C> .
     * @return triple if inputs
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    public static <A, B, C> @NotNull Triple<A, B, C>
    of(A first, B second, C third) {
        return new Triple<>(first, second, third);
    }

    /**
     * @param function triple function
     * @param <T> output type
     * @return T from collapsing triple function
     */
    public <T> T then(@NotNull Function<Triple<A, B, C>, T> function) {
        return function.apply(this);
    }

    /**
     * @param function tri function
     * @param <T> output type
     * @return T from collapsing tri types
     */
    public <T> T then(@NotNull TriFunction<A, B, C, T> function) {
        return function.apply(first, second, third);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;
        return Objects.equals(first, triple.first) &&
                Objects.equals(second, triple.second) &&
                Objects.equals(third, triple.third);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second, third);
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return "Triple{" +
                "first=" + first +
                ", second=" + second +
                ", third=" + third +
                '}';
    }
}
