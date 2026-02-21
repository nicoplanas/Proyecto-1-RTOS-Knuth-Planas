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
        engine.startInterrupt(message, cycles);
        try {
            Thread.sleep(Math.max(1, cycles * cycleMs));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        engine.endInterrupt();
    }
}
