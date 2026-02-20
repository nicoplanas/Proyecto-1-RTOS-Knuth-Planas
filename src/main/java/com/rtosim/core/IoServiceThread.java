package com.rtosim.core;

import com.rtosim.model.Pcb;

public class IoServiceThread extends Thread {
    private final SimulatorEngine engine;
    private final Pcb pcb;
    private final int cycles;
    private final int cycleMs;

    public IoServiceThread(SimulatorEngine engine, Pcb pcb, int cycles, int cycleMs) {
        this.engine = engine;
        this.pcb = pcb;
        this.cycles = cycles;
        this.cycleMs = cycleMs;
        setName("IO-" + pcb.getId());
    }

    @Override
    public void run() {
        try {
            Thread.sleep(Math.max(1, cycles * cycleMs));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        
    }
}
