package com.rtosim.struct;

public class SimpleLinkedList<T> {
    private static class Node<T> {
        private final T value;
        private Node<T> next;

        private Node(T value) {
            this.value = value;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public void addLast(T value) {
        Node<T> node = new Node<>(value);
        if (tail == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size += 1;
    }

    public void addFirst(T value) {
        Node<T> node = new Node<>(value);
        if (head == null) {
            head = node;
            tail = node;
        } else {
            node.next = head;
            head = node;
        }
        size += 1;
    }

    public T removeFirst() {
        if (head == null) {
            return null;
        }
        Node<T> node = head;
        head = head.next;
        if (head == null) {
            tail = null;
        }
        size -= 1;
        return node.value;
    }

    public T removeAt(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        if (index == 0) {
            return removeFirst();
        }
        Node<T> prev = head;
        for (int i = 0; i < index - 1; i += 1) {
            prev = prev.next;
        }
        Node<T> current = prev.next;
        prev.next = current.next;
        if (current == tail) {
            tail = prev;
        }
        size -= 1;
        return current.value;
    }

    public boolean remove(T value) {
        if (head == null) {
            return false;
        }
        if (head.value == value) {
            removeFirst();
            return true;
        }
        Node<T> prev = head;
        Node<T> current = head.next;
        while (current != null) {
            if (current.value == value) {
                prev.next = current.next;
                if (current == tail) {
                    tail = prev;
                }
                size -= 1;
                return true;
            }
            prev = current;
            current = current.next;
        }
        return false;
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        Node<T> current = head;
        for (int i = 0; i < index; i += 1) {
            current = current.next;
        }
        return current.value;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }
}
