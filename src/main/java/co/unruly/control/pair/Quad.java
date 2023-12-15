package co.unruly.control.pair;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * @param first .
 * @param second .
 * @param third .
 * @param fourth .
 * @param <A> .
 * @param <B> .
 * @param <C> .
 * @param <D> .
 */
public record Quad<A, B, C, D>(A first, B second, C third, D fourth) {

    /**
     * @param <A> .
     * @param <B> .
     * @param <C> .
     * @param <D> .
     * @param <T> output type
     */
    @FunctionalInterface
    public interface QuadFunction<A, B, C, D, T> {
        /**
         * @param a .
         * @param b .
         * @param c .
         * @param d .
         * @return T
         */
        T apply(A a, B b, C c, D d);
    }

    /**
     * @param first .
     * @param second .
     * @param third .
     * @param fourth .
     * @param <A> .
     * @param <B> .
     * @param <C> .
     * @param <D> .
     * @return new quad of first-fourth params
     */
    @Contract("_, _, _, _ -> new")
    public static <A, B, C, D> @NotNull Quad<A, B, C, D> of(A first, B second, C third, D fourth) {
        return new Quad<>(first, second, third, fourth);
    }

    /**
     * @param function quad function to use
     * @param <T> return type
     * @return T given this
     */
    public <T> T then(@NotNull Function<Quad<A, B, C, D>, T> function) {
        return function.apply(this);
    }

    /**
     * @param function quad function to use
     * @param <T> return type
     * @return T
     */
    public <T> T then(@NotNull QuadFunction<A, B, C, D, T> function) {
        return function.apply(first, second, third, fourth);
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return "Quad{" +
                "first=" + first +
                ", second=" + second +
                ", third=" + third +
                ", fourth=" + fourth +
                '}';
    }
}
