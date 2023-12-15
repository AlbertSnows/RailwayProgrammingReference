package co.unruly.control.validation;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FailedValidation<?, ?> that = (FailedValidation<?, ?>) o;
        return Objects.equals(value, that.value) &&
                Objects.equals(errors, that.errors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, errors);
    }



    @Override
    public List<E> delegate() {
        return errors;
    }
}
