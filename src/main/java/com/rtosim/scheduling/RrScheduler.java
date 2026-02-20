package com.rtosim.scheduling;

import com.rtosim.model.Pcb;
import com.rtosim.struct.SimpleQueue;

public class RrScheduler implements SchedulingPolicy {
    private final int quantum;

    public RrScheduler(int quantum) {
        this.quantum = Math.max(1, quantum);
    }

    @Override
    public PolicyType getType() {
        return PolicyType.RR;
    }

    @Override
    public boolean isPreemptive() {
        return true;
    }

    @Override
    public boolean isRoundRobin() {
        return true;
    }

    @Override
    public int getQuantum() {
        return quantum;
    }

    @Override
    public int pickNextIndex(SimpleQueue<Pcb> readyQueue, int clockTick) {
        return readyQueue.isEmpty() ? -1 : 0;
    }

    @Override
    public boolean shouldPreempt(Pcb current, SimpleQueue<Pcb> readyQueue, int clockTick) {
        return false;
    }
}
