package com.rtosim.io;

import com.rtosim.model.Pcb;
import com.rtosim.struct.SimpleQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class CsvProcessLoader implements ProcessLoader {
    @Override
    public SimpleQueue<Pcb> load(File file, int startId, int clockTick) throws Exception {
        SimpleQueue<Pcb> queue = new SimpleQueue<>();
        int id = startId;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("name")) {
                    continue;
                }
                String[] parts = trimmed.split(",");
                if (parts.length < 5) {
                    continue;
                }
                String name = parts[0].trim();
                int instructions = parseInt(parts, 1, 10);
                int priority = parseInt(parts, 2, 5);
                int period = parseInt(parts, 3, 10);
                int deadline = parseInt(parts, 4, instructions + 10);
                int ioEvery = parseInt(parts, 5, 0);
                int ioService = parseInt(parts, 6, 0);
                instructions = Math.max(1, instructions);
                priority = clamp(priority, 1, 10);
                period = Math.max(1, period);
                deadline = Math.max(instructions, deadline);
                ioEvery = Math.max(0, ioEvery);
                ioService = ioEvery > 0 ? Math.max(1, ioService) : 0;
                Pcb pcb = new Pcb(id, name, instructions, priority, period, deadline,
                        ioEvery, ioService, clockTick);
                queue.enqueue(pcb);
                id += 1;
            }
        }
        return queue;
    }

    private int parseInt(String[] parts, int index, int defaultValue) {
        if (index >= parts.length) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(parts[index].trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}
