package core;

/**
 * LIFO stack implementation backed by DynamicArray.
 *
 * @param <T> Element type
 */
public class ArrayStack<T> {
    private final DynamicArray<T> array;

    public ArrayStack() {
        this.array = new DynamicArray<>();
    }

    public ArrayStack(int initialCapacity) {
        this.array = new DynamicArray<>(initialCapacity);
    }

    public void push(T item) {
        array.add(item);
    }

    public T pop() {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot pop from an empty stack");
        }
        return array.remove(array.size() - 1);
    }

    public T peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot peek an empty stack");
        }
        return array.get(array.size() - 1);
    }

    public boolean isEmpty() {
        return array.isEmpty();
    }

    public int size() {
        return array.size();
    }

    public void clear() {
        array.clear();
    }
}
