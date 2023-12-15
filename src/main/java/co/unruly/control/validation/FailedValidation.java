package co.unruly.control.validation;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @param value  value under consideration
 * @param errors errors associated with value
 * @param <T> value type
 * @param <E> error type
 */
public record FailedValidation<T, E>(T value, List<E> errors) implements ForwardingList<E> {

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return "FailedValidation{" +
                "value=" + value +
                ", errors=" + errors +
                '}';
    }

    @Override
    public List<E> delegate() {
        return errors;
    }
}
