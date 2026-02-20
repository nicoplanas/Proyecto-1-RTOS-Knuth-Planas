package com.rtosim.scheduling;

import com.rtosim.model.Pcb;
import com.rtosim.struct.SimpleQueue;

public class SrtScheduler implements SchedulingPolicy {
    @Override
    public PolicyType getType() {
        return PolicyType.SRT;
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
        int bestRemaining = Integer.MAX_VALUE;
        for (int i = 0; i < readyQueue.size(); i += 1) {
            Pcb pcb = readyQueue.get(i);
            if (pcb != null && pcb.getRemainingInstructions() < bestRemaining) {
                bestRemaining = pcb.getRemainingInstructions();
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
        int currentRemaining = current.getRemainingInstructions();
        for (int i = 0; i < readyQueue.size(); i += 1) {
            Pcb pcb = readyQueue.get(i);
            if (pcb != null && pcb.getRemainingInstructions() < currentRemaining) {
                return true;
            }
        }
        return false;
    }
}
