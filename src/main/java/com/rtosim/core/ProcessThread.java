package com.rtosim.core;

import com.rtosim.model.CycleEvent;
import com.rtosim.model.Pcb;
import java.util.concurrent.Semaphore;

public class ProcessThread extends Thread {
    private final Pcb pcb;
    private final Semaphore runPermit;
    private final Semaphore cycleDone;
    private volatile boolean running;

    public ProcessThread(Pcb pcb) {
        this.pcb = pcb;
        this.runPermit = new Semaphore(0);
        this.cycleDone = new Semaphore(0);
        this.running = true;
        setName("Process-" + pcb.getId());
    }

    public void requestStop() {
        running = false;
        runPermit.release();
    }

    public void runOneCycle() throws InterruptedException {
        runPermit.release();
        cycleDone.acquire();
    }

    public CycleEvent getLastEvent() {
        return pcb.getLastEvent();
    }

    @Override
    public void run() {
        while (running) {
            try {
                runPermit.acquire();
            } catch (InterruptedException ex) {
                continue;
            }
            if (!running) {
                break;
            }
            pcb.stepCpuCycle();
            cycleDone.release();
        }
    }
}
