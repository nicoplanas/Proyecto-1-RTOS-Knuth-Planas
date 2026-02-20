package com.rtosim.scheduling;

import com.rtosim.model.Pcb;
import com.rtosim.struct.SimpleQueue;

public class EdfScheduler implements SchedulingPolicy {
    @Override
    public PolicyType getType() {
        return PolicyType.EDF;
    }

    @Override
    public boolean isPreemptive() {
        return true;
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
        int bestIndex = -1;
        int bestDeadline = Integer.MAX_VALUE;
        for (int i = 0; i < readyQueue.size(); i += 1) {
            Pcb pcb = readyQueue.get(i);
            if (pcb != null && pcb.getDeadlineRemaining() < bestDeadline) {
                bestDeadline = pcb.getDeadlineRemaining();
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    @Override
    public boolean shouldPreempt(Pcb current, SimpleQueue<Pcb> readyQueue, int clockTick) {
        if (current == null) {
            return false;
        }
        int currentDeadline = current.getDeadlineRemaining();
        for (int i = 0; i < readyQueue.size(); i += 1) {
            Pcb pcb = readyQueue.get(i);
            if (pcb != null && pcb.getDeadlineRemaining() < currentDeadline) {
                return true;
            }
        }
        return false;
    }
}
