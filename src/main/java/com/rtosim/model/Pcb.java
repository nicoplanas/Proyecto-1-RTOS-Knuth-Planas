package com.rtosim.model;

public class Pcb {
    private final int id;
    private final String name;
    private final int totalInstructions;
    private int remainingInstructions;
    private int priority;
    private int period;
    private int deadline;
    private int deadlineRemaining;
    private int pc;
    private int mar;
    private int ioEvery;
    private int ioServiceCycles;
    private int ioCountdown;
    private ProcessState state;
    private CycleEvent lastEvent;
    private int arrivalTick;
    private int startTick;
    private int finishTick;
    private int totalWaitTime;
    private int lastReadyTick;

    public Pcb(int id, String name, int instructions, int priority, int period, int deadline,
            int ioEvery, int ioServiceCycles, int arrivalTick) {
        this.id = id;
        this.name = name;
        this.totalInstructions = instructions;
        this.remainingInstructions = instructions;
        this.priority = priority;
        this.period = period;
        this.deadline = deadline;
        this.deadlineRemaining = deadline;
        this.ioEvery = ioEvery;
        this.ioServiceCycles = ioServiceCycles;
        this.ioCountdown = ioEvery > 0 ? ioEvery : -1;
        this.state = ProcessState.NEW;
        this.lastEvent = CycleEvent.CONTINUE;
        this.arrivalTick = arrivalTick;
        this.startTick = -1;
        this.finishTick = -1;
        this.totalWaitTime = 0;
        this.lastReadyTick = arrivalTick;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTotalInstructions() {
        return totalInstructions;
    }

    public int getRemainingInstructions() {
        return remainingInstructions;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getDeadline() {
        return deadline;
    }

    public void setDeadline(int deadline) {
        this.deadline = deadline;
    }

    public int getDeadlineRemaining() {
        return deadlineRemaining;
    }

    public void setDeadlineRemaining(int deadlineRemaining) {
        this.deadlineRemaining = deadlineRemaining;
    }

    public int getPc() {
        return pc;
    }

    public int getMar() {
        return mar;
    }

    public int getIoEvery() {
        return ioEvery;
    }

    public int getIoServiceCycles() {
        return ioServiceCycles;
    }

    public void setIoConfig(int ioEvery, int ioServiceCycles) {
        this.ioEvery = ioEvery;
        this.ioServiceCycles = ioServiceCycles;
        this.ioCountdown = ioEvery > 0 ? ioEvery : -1;
    }

    public ProcessState getState() {
        return state;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }

    public CycleEvent getLastEvent() {
        return lastEvent;
    }

    public int getArrivalTick() {
        return arrivalTick;
    }

    public void markReady(int clockTick) {
        this.lastReadyTick = clockTick;
    }

    public void markRunning(int clockTick) {
        if (startTick < 0) {
            startTick = clockTick;
        }
        totalWaitTime += Math.max(0, clockTick - lastReadyTick);
    }

    public void markFinished(int clockTick) {
        finishTick = clockTick;
    }

    public int getFinishTick() {
        return finishTick;
    }

    public int getTotalWaitTime() {
        return totalWaitTime;
    }

    public int getTurnaroundTime() {
        if (finishTick < 0) {
            return 0;
        }
        return finishTick - arrivalTick;
    }

    public void updateDeadlineRemaining() {
        deadlineRemaining -= 1;
    }

    public boolean metDeadline() {
        if (finishTick < 0) {
            return false;
        }
        return (finishTick - arrivalTick) <= deadline;
    }

    public CycleEvent stepCpuCycle() {
        if (remainingInstructions <= 0) {
            lastEvent = CycleEvent.FINISHED;
            return lastEvent;
        }
        remainingInstructions -= 1;
        pc += 1;
        mar += 1;

        if (ioEvery > 0) {
            ioCountdown -= 1;
            if (ioCountdown == 0 && remainingInstructions > 0) {
                ioCountdown = ioEvery;
                lastEvent = CycleEvent.IO_BLOCK;
                return lastEvent;
            }
        }

        if (remainingInstructions == 0) {
            lastEvent = CycleEvent.FINISHED;
        } else {
            lastEvent = CycleEvent.CONTINUE;
        }
        return lastEvent;
    }
}
