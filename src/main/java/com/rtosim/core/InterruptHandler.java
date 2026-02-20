package com.rtosim.core;

public class InterruptHandler extends Thread {
    private final SimulatorEngine engine;
    private final String message;
    private final int cycles;
    private final int cycleMs;

    public InterruptHandler(SimulatorEngine engine, String message, int cycles, int cycleMs) {
        this.engine = engine;
        this.message = message;
        this.cycles = cycles;
        this.cycleMs = cycleMs;
        setName("Interrupt-Handler");
    }

    @Override
    public void run() {
    }
}
