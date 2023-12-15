package co.unruly.control;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Interface for working with optionals
 */
@SuppressWarnings({"unused", "optional", "OptionalUsedAsFieldOrParameterType"})
public interface Optionals {

    /**
     * Converts an Optional to a Stream.
     * If the Optional was present, you get a Stream of one item.
     * If it was absent, you get an empty stream.
     * <p>
     * Flat mapping a Stream of Optionals over this method will return a Stream of the
     * contents of the present Optionals in the input stream.
     * @param item optional item
     * @param <T> item type
     * @return stream of T's or empty's
     */
    static <T> Stream<T> stream(Optional<T> item) {
        return either(item, Stream::of, Stream::empty);
    }

    /**
     * If the provided optional is present, applies the first function to the wrapped value and returns it.
     * Otherwise, returns the value supplied by the second function
     * @param optional optional value
     * @param onPresent action on present
     * @param onAbsent action on absence
     * @param <T> optional type
     * @param <R> result type
     * @return result type from either onPresent on onAbsent
     */
    static <T, R> R either(@NotNull Optional<T> optional, Function<T, R> onPresent, Supplier<R> onAbsent) {
        return optional.map(onPresent).orElseGet(onAbsent);
    }

    /**
     * If the provided Optional is present, pass the wrapped value to the provided consumer
     * <p>
     * This simply invokes Optional.ifPresent(), and exists for cases where side-effects are required
     * in both the present and absent cases on an Optional. The Optional API doesn't cover the latter case,
     * so this provides a mimicking calling convention to permit consistency.
     * @param optional optional value
     * @param consume consuming function
     * @param <T> value type
     */
    static <T> void ifPresent(@NotNull Optional<T> optional, Consumer<T> consume) {
        optional.ifPresent(consume);
    }

    /**
     * If the provided Optional is empty, invoke the provided Runnable
     * @param optional optional value
     * @param action absent action
     * @param <T> value type
     */
    static <T> void ifAbsent(@NotNull Optional<T> optional, Runnable action) {
        if(optional.isEmpty()) {
            action.run();
        }
    }
}
