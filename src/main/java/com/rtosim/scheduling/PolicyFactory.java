package com.rtosim.scheduling;

public class PolicyFactory {
    public SchedulingPolicy create(PolicyType type, int quantum) {
        return new FcfScheduler();
    }
}
