package com.rtosim.core;

public class ClockThread extends Thread {
    private final SimulatorEngine engine;
    private volatile boolean running;

    public ClockThread(SimulatorEngine engine) {
        this.engine = engine;
        this.running = true;
        setName("System-Clock");
    }

    public void requestStop() {
        running = false;
        interrupt();
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(Math.max(1, engine.getCycleMs()));
            } catch (InterruptedException ex) {
                if (!running) {
                    break;
                }
            }
            engine.tick();
        }
    }
}
