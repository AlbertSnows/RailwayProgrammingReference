package co.unruly.control.validation;

import co.unruly.control.result.Result;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @param <T> input
 * @param <E> errors
 */
@FunctionalInterface
public interface Validator<T, E> extends Function<T, Result<T, FailedValidation<T, E>>> {

    default Result<T, FailedValidation<T, E>> apply(T item) {
        List<E> errors = validate(item).collect(toList());
        return errors.isEmpty()
            ? Result.success(item)
            : Result.failure(new FailedValidation<>(item, errors));
    }

    /**
     * @param item input
     * @return stream of errors E
     */
    Stream<E> validate(T item);

}
