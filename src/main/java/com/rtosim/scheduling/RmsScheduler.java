package com.rtosim.scheduling;

import com.rtosim.model.Pcb;
import com.rtosim.struct.SimpleQueue;

public class RmsScheduler implements SchedulingPolicy {
    @Override
    public PolicyType getType() {
        return PolicyType.RMS;
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
        int bestPeriod = Integer.MAX_VALUE;
        for (int i = 0; i < readyQueue.size(); i += 1) {
            Pcb pcb = readyQueue.get(i);
            if (pcb != null && pcb.getPeriod() > 0 && pcb.getPeriod() < bestPeriod) {
                bestPeriod = pcb.getPeriod();
                bestIndex = i;
            }
        }
        if (bestIndex == -1) {
            return readyQueue.isEmpty() ? -1 : 0;
        }
        return bestIndex;
    }

    @Override
    public boolean shouldPreempt(Pcb current, SimpleQueue<Pcb> readyQueue, int clockTick) {
        if (current == null) {
            return false;
        }
        int currentPeriod = current.getPeriod();
        for (int i = 0; i < readyQueue.size(); i += 1) {
            Pcb pcb = readyQueue.get(i);
            if (pcb != null && pcb.getPeriod() > 0 && pcb.getPeriod() < currentPeriod) {
                return true;
            }
        }
        return false;
    }
}
