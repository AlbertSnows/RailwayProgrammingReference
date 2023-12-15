package co.unruly.control.validation;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * @param <T> list type
 *           here is a link to forward lists in C++
 *           <a href="https://cplusplus.com/reference/forward_list/forward_list/">link</a>
 *           It /should/ be the same principle
 */
public interface ForwardingList<T> extends List<T> {

    /**
     * @return delegate list
     */
    List<T> delegate();

    default int size() {
        return delegate().size();
    }

    default boolean isEmpty() {
        return delegate().isEmpty();
    }

    default boolean contains(Object o) {
        return delegate().contains(o);
    }

    default @NotNull Iterator<T> iterator() {
        return delegate().iterator();
    }

    default Object @NotNull [] toArray() {
        return delegate().toArray();
    }

    default <T1> T1 @NotNull [] toArray(T1 @NotNull [] a) {
        return delegate().toArray(a);
    }

    default boolean add(T t) {
        return delegate().add(t);
    }

    default boolean remove(Object o) {
        return delegate().remove(o);
    }

    default boolean containsAll(@NotNull Collection<?> c) {
        return new HashSet<>(delegate()).containsAll(c);
    }

    default boolean addAll(@NotNull Collection<? extends T> c) {
        return delegate().addAll(c);
    }

    default boolean addAll(int index, @NotNull Collection<? extends T> c) {
        return delegate().addAll(index, c);
    }

    default boolean removeAll(@NotNull Collection<?> c) {
        return delegate().removeAll(c);
    }

    default boolean retainAll(@NotNull Collection<?> c) {
        return delegate().retainAll(c);
    }

    default void replaceAll(UnaryOperator<T> operator) {
        delegate().replaceAll(operator);
    }

    default void sort(Comparator<? super T> c) {
        delegate().sort(c);
    }

    default void clear() {
        delegate().clear();
    }

    default T get(int index) {
        return delegate().get(index);
    }

    default T set(int index, T element) {
        return delegate().set(index, element);
    }

    default void add(int index, T element) {
        delegate().add(index, element);
    }

    default T remove(int index) {
        return delegate().remove(index);
    }

    default int indexOf(Object o) {
        return delegate().indexOf(o);
    }

    default int lastIndexOf(Object o) {
        return delegate().lastIndexOf(o);
    }

    default @NotNull ListIterator<T> listIterator() {
        return delegate().listIterator();
    }

    default @NotNull ListIterator<T> listIterator(int index) {
        return delegate().listIterator(index);
    }

    default @NotNull List<T> subList(int fromIndex, int toIndex) {
        return delegate().subList(fromIndex, toIndex);
    }

    default Spliterator<T> spliterator() {
        return delegate().spliterator();
    }

    default boolean removeIf(Predicate<? super T> filter) {
        return delegate().removeIf(filter);
    }

    default Stream<T> stream() {
        return delegate().stream();
    }

    default Stream<T> parallelStream() {
        return delegate().parallelStream();
    }

    default void forEach(Consumer<? super T> action) {
        delegate().forEach(action);
    }
}
