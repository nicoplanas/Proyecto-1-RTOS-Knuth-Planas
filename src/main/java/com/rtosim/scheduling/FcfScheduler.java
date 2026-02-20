package com.rtosim.scheduling;

import com.rtosim.model.Pcb;
import com.rtosim.struct.SimpleQueue;

public class FcfScheduler implements SchedulingPolicy {
    @Override
    public PolicyType getType() {
        return PolicyType.FCFS;
    }

    @Override
    public boolean isPreemptive() {
        return false;
    }

    @Override
    public boolean isRoundRobin() {
        return false;
    }

    @Override
    public int getQuantum() {
        return 0;
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
