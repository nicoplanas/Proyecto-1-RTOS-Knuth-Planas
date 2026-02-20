package com.rtosim.io;

import com.rtosim.model.Pcb;
import com.rtosim.struct.SimpleQueue;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class JsonProcessLoader implements ProcessLoader {
    @Override
    public SimpleQueue<Pcb> load(File file, int startId, int clockTick) throws Exception {
        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        SimpleQueue<Pcb> queue = new SimpleQueue<>();
        int id = startId;
        int index = 0;
        while (index < content.length()) {
            int start = content.indexOf('{', index);
            if (start < 0) {
                break;
            }
            int end = content.indexOf('}', start + 1);
            if (end < 0) {
                break;
            }
            String obj = content.substring(start + 1, end);
            String name = extractString(obj, "name", "P" + id);
            int instructions = extractInt(obj, "instructions", 10);
            int priority = extractInt(obj, "priority", 5);
            int period = extractInt(obj, "period", 10);
            int deadline = extractInt(obj, "deadline", instructions + 10);
            int ioEvery = extractInt(obj, "ioEvery", 0);
            int ioService = extractInt(obj, "ioService", 0);
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
            index = end + 1;
        }
        return queue;
    }

    private String extractString(String obj, String key, String defaultValue) {
        int keyIndex = obj.indexOf('"' + key + '"');
        if (keyIndex < 0) {
            return defaultValue;
        }
        int colon = obj.indexOf(':', keyIndex);
        if (colon < 0) {
            return defaultValue;
        }
        int firstQuote = obj.indexOf('"', colon + 1);
        if (firstQuote < 0) {
            return defaultValue;
        }
        int secondQuote = obj.indexOf('"', firstQuote + 1);
        if (secondQuote < 0) {
            return defaultValue;
        }
        return obj.substring(firstQuote + 1, secondQuote).trim();
    }

    private int extractInt(String obj, String key, int defaultValue) {
        int keyIndex = obj.indexOf('"' + key + '"');
        if (keyIndex < 0) {
            return defaultValue;
        }
        int colon = obj.indexOf(':', keyIndex);
        if (colon < 0) {
            return defaultValue;
        }
        int start = colon + 1;
        while (start < obj.length() && Character.isWhitespace(obj.charAt(start))) {
            start += 1;
        }
        int end = start;
        while (end < obj.length() && (Character.isDigit(obj.charAt(end)) || obj.charAt(end) == '-')) {
            end += 1;
        }
        if (start == end) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(obj.substring(start, end));
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
