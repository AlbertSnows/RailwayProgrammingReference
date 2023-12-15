package co.unruly.control;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Created by tomj on 31/03/2017.
 */
@SuppressWarnings("unused")
public interface Predicates {

    /**
     * Negates a predicate: mostly useful when our predicate is a method reference or lambda where we can't
     * call negate() on it directly, or where the code reads better by having the negation at the beginning
     * rather than the end.
     * @param test predicate function
     * @param <T> input type
     * @return reverse of the predicate function (negation)
     */
    @Contract(pure = true)
    static <T> @NotNull Predicate<T> not(@NotNull Predicate<T> test) {
        return test.negate();
    }
}
