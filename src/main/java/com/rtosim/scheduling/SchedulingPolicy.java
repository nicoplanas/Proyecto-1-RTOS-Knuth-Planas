package com.rtosim.scheduling;

import com.rtosim.model.Pcb;
import com.rtosim.struct.SimpleQueue;

public interface SchedulingPolicy {
    PolicyType getType();

    boolean isPreemptive();

    boolean isRoundRobin();

    int getQuantum();

    int pickNextIndex(SimpleQueue<Pcb> readyQueue, int clockTick);

    boolean shouldPreempt(Pcb current, SimpleQueue<Pcb> readyQueue, int clockTick);
}
