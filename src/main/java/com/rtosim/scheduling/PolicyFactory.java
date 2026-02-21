package com.rtosim.scheduling;

public class PolicyFactory {
    public SchedulingPolicy create(PolicyType type, int quantum) {
        if (type == PolicyType.RR) {
            return new RrScheduler(quantum);
        }
        if (type == PolicyType.SRT) {
            return new SrtScheduler();
        }
        if (type == PolicyType.STATIC_PRIORITY) {
            return new StaticPriorityScheduler();
        }
        if (type == PolicyType.RMS) {
            return new RmsScheduler();
        }
        if (type == PolicyType.EDF) {
            return new EdfScheduler();
        }
        return new FcfScheduler();
    }
}
