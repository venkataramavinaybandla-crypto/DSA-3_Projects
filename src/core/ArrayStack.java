package core;

/**
<<<<<<< HEAD
 * Generic LIFO Stack implementation backed by DynamicArray.
 *
 * @param <T> the type of elements stored in the stack
 */
public class ArrayStack<T> {
    private final DynamicArray<T> elements;

    public ArrayStack() {
        this.elements = new DynamicArray<>();
    }

    public ArrayStack(int initialCapacity) {
        this.elements = new DynamicArray<>(initialCapacity);
    }

    public void push(T item) {
        elements.add(item);
=======
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
>>>>>>> 6e0fdd0e0f1f50ed515e46abe8b8efb1cd9ebd0e
    }

    public T pop() {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot pop from an empty stack");
        }
<<<<<<< HEAD
        return elements.remove(elements.size() - 1);
=======
        return array.remove(array.size() - 1);
>>>>>>> 6e0fdd0e0f1f50ed515e46abe8b8efb1cd9ebd0e
    }

    public T peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot peek an empty stack");
        }
<<<<<<< HEAD
        return elements.get(elements.size() - 1);
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public int size() {
        return elements.size();
    }

    public void clear() {
        elements.clear();
    }

    @Override
    public String toString() {
        return elements.toString();
=======
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
>>>>>>> 6e0fdd0e0f1f50ed515e46abe8b8efb1cd9ebd0e
    }
}
