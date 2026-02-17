package com.rtosim.struct;

public class DoubleSeries {
    private double[] data;
    private int size;

    public DoubleSeries(int capacity) {
        if (capacity <= 0) {
            capacity = 32;
        }
        data = new double[capacity];
        size = 0;
    }

    public void add(double value) {
        if (size >= data.length) {
            double[] next = new double[data.length * 2];
            for (int i = 0; i < data.length; i += 1) {
                next[i] = data[i];
            }
            data = next;
        }
        data[size] = value;
        size += 1;
    }

    public int size() {
        return size;
    }

    public double get(int index) {
        if (index < 0 || index >= size) {
            return 0.0;
        }
        return data[index];
    }

    public double max() {
        double max = 0.0;
        for (int i = 0; i < size; i += 1) {
            if (data[i] > max) {
                max = data[i];
            }
        }
        return max;
    }

    public void clear() {
        size = 0;
    }
}
