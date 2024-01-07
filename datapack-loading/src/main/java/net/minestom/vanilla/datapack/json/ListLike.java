package net.minestom.vanilla.datapack.json;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public interface ListLike<T> extends List<T> {

    /**
     * Returns a list containing all of the elements in this list, in proper sequence.
     */
    @NotNull List<T> list();

    @Override
    default int size() {
        return list().size();
    }

    @Override
    default boolean isEmpty() {
        return list().isEmpty();
    }

    @Override
    default boolean contains(Object o) {
        return list().contains(o);
    }

    @NotNull
    @Override
    default Iterator<T> iterator() {
        return list().iterator();
    }

    @NotNull
    @Override
    default Object @NotNull [] toArray() {
        return list().toArray();
    }

    @NotNull
    @Override
    default <T1> T1 @NotNull [] toArray(@NotNull T1 @NotNull [] a) {
        return list().toArray(a);
    }

    @Override
    default boolean add(T t) {
        return list().add(t);
    }

    @Override
    default boolean remove(Object o) {
        return list().remove(o);
    }

    @Override
    default boolean containsAll(@NotNull Collection<?> c) {
        //noinspection SlowListContainsAll
        return list().containsAll(c);
    }

    @Override
    default boolean addAll(@NotNull Collection<? extends T> c) {
        return list().addAll(c);
    }

    @Override
    default boolean addAll(int index, @NotNull Collection<? extends T> c) {
        return list().addAll(index, c);
    }

    @Override
    default boolean removeAll(@NotNull Collection<?> c) {
        return list().removeAll(c);
    }

    @Override
    default boolean retainAll(@NotNull Collection<?> c) {
        return list().retainAll(c);
    }

    @Override
    default void clear() {
        list().clear();
    }

    @Override
    default T get(int index) {
        return list().get(index);
    }

    @Override
    default T set(int index, T element) {
        return list().set(index, element);
    }

    @Override
    default void add(int index, T element) {
        list().add(index, element);
    }

    @Override
    default T remove(int index) {
        return list().remove(index);
    }

    @Override
    default int indexOf(Object o) {
        return list().indexOf(o);
    }

    @Override
    default int lastIndexOf(Object o) {
        return list().lastIndexOf(o);
    }

    @NotNull
    @Override
    default ListIterator<T> listIterator() {
        return list().listIterator();
    }

    @NotNull
    @Override
    default ListIterator<T> listIterator(int index) {
        return list().listIterator(index);
    }

    @NotNull
    @Override
    default List<T> subList(int fromIndex, int toIndex) {
        return list().subList(fromIndex, toIndex);
    }
}
