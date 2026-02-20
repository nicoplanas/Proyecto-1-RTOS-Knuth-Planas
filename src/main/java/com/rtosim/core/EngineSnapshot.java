package com.rtosim.core;

import com.rtosim.model.Pcb;
import com.rtosim.scheduling.PolicyType;

public class EngineSnapshot {
    private final int clockTick;
    private final boolean osBusy;
    private final PolicyType policyType;
    private final double cpuUtilization;
    private final double missionSuccessRate;
    private final double throughput;
    private final double averageWaitTime;
    private final Pcb[] newQueue;
    private final Pcb[] readyQueue;
    private final Pcb[] blockedQueue;
    private final Pcb[] readySuspendedQueue;
    private final Pcb[] blockedSuspendedQueue;
    private final Pcb[] terminatedQueue;
    private final Pcb[] running;

    public EngineSnapshot(int clockTick, boolean osBusy, PolicyType policyType,
            double cpuUtilization, double missionSuccessRate, double throughput,
            double averageWaitTime, Pcb[] newQueue, Pcb[] readyQueue, Pcb[] blockedQueue,
            Pcb[] readySuspendedQueue, Pcb[] blockedSuspendedQueue, Pcb[] terminatedQueue,
            Pcb[] running) {
        this.clockTick = clockTick;
        this.osBusy = osBusy;
        this.policyType = policyType;
        this.cpuUtilization = cpuUtilization;
        this.missionSuccessRate = missionSuccessRate;
        this.throughput = throughput;
        this.averageWaitTime = averageWaitTime;
        this.newQueue = newQueue;
        this.readyQueue = readyQueue;
        this.blockedQueue = blockedQueue;
        this.readySuspendedQueue = readySuspendedQueue;
        this.blockedSuspendedQueue = blockedSuspendedQueue;
        this.terminatedQueue = terminatedQueue;
        this.running = running;
    }

    public int getClockTick() {
        return clockTick;
    }

    public boolean isOsBusy() {
        return osBusy;
    }

    public PolicyType getPolicyType() {
        return policyType;
    }

    public double getCpuUtilization() {
        return cpuUtilization;
    }

    public double getMissionSuccessRate() {
        return missionSuccessRate;
    }

    public double getThroughput() {
        return throughput;
    }

    public double getAverageWaitTime() {
        return averageWaitTime;
    }

    public Pcb[] getNewQueue() {
        return newQueue;
    }

    public Pcb[] getReadyQueue() {
        return readyQueue;
    }

    public Pcb[] getBlockedQueue() {
        return blockedQueue;
    }

    public Pcb[] getReadySuspendedQueue() {
        return readySuspendedQueue;
    }

    public Pcb[] getBlockedSuspendedQueue() {
        return blockedSuspendedQueue;
    }

    public Pcb[] getTerminatedQueue() {
        return terminatedQueue;
    }

    public Pcb[] getRunning() {
        return running;
    }
}
