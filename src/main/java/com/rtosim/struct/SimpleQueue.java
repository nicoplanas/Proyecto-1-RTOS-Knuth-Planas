package com.rtosim.struct;

public class SimpleQueue<T> {
    private final SimpleLinkedList<T> list = new SimpleLinkedList<>();

    public void enqueue(T value) {
        list.addLast(value);
    }

    public T dequeue() {
        return list.removeFirst();
    }

    public T removeAt(int index) {
        return list.removeAt(index);
    }

    public boolean remove(T value) {
        return list.remove(value);
    }

    public T get(int index) {
        return list.get(index);
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public void clear() {
        list.clear();
    }
}
