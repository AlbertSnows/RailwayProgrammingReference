package co.unruly.control.pair;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Convenience methods for defining maps inline.
 */
public interface Maps {

    /**
     * Build a map from the provided key-value pairs. For example:
     * <pre>
     * {@code
     * Map<String, Int> lettersInWords = mapOf(
     *   entry("Hello", 5),
     *   entry("Goodbye", 7)
     * );
     * }
     * </pre>
     * @param entries the map entries
     * @param <K> the key type
     * @param <V> the value type
     * @return a map containing all the provided key-value pairs
     */
    @SafeVarargs
    static <K, V> Map<K, V> mapOf(Pair<K, V> ...entries) {
        return Stream.of(entries).collect(toMap());
    }

    /**
     * Collects a stream of pairs into a map
     * @param <K> the left type of the pair, interpreted as the key type
     * @param <V> the right type of the pair, interpreted as the value type
     * @return a Collector which collects a Stream of Pairs into a Map
     */
    @Contract(value = " -> new", pure = true)
    static <K, V> @NotNull Collector<Pair<K, V>, ?, Map<K, V>> toMap() {
        return Collectors.toMap(Pair::left, Pair::right);
    }

    /**
     * Creates a key-value pair.
     * <p>
     * This is just an alias for Pair. Of, that makes more sense in a map-initialisation context.
     * @param key the key
     * @param value the value
     * @param <K> the key type
     * @param <V> the value type
     * @return a key-value pair
     */
    @Contract(value = "_, _ -> new", pure = true)
    static <K, V> @NotNull Pair<K, V> entry(K key, V value) {
        return Pair.of(key, value);
    }
}
