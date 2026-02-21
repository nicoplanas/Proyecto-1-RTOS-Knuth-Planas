package com.rtosim.core; // Definimos el paquete donde vive esta clase

// Importamos las clases necesarias de otros paquetes
import com.rtosim.model.CycleEvent; // Lo que un proceso devuelve después de ejecutar una instrucción
import com.rtosim.model.Pcb; // El Process Control Block - la estructura de datos que representa un proceso
import com.rtosim.model.ProcessState; // Un enum con los estados posibles (NUEVO, LISTO, EJECUCIÓN,...)

// Las clases para implementar los algoritmos de planificación
import com.rtosim.scheduling.PolicyFactory;
import com.rtosim.scheduling.PolicyType;
import com.rtosim.scheduling.SchedulingPolicy;

// Nuestras estructuras de datos
import com.rtosim.struct.SimpleLinkedList;
import com.rtosim.struct.SimpleQueue;

import java.util.Random; // Para generar números aleatorios (crear procesos aleatorios)
import java.util.concurrent.Semaphore; // Para exclusión mutua

public class SimulatorEngine {
    
    // Colas para los estados del proceso
    private final SimpleQueue<Pcb> newQueue;
    private final SimpleQueue<Pcb> readyQueue;
    private final SimpleQueue<Pcb> blockedQueue;
    private final SimpleQueue<Pcb> terminatedQueue;
    private final SimpleQueue<Pcb> readySuspendedQueue;
    private final SimpleQueue<Pcb> blockedSuspendedQueue;
    
    // Lista de todos los hilos (threads) de proceso que están activos
    private final SimpleLinkedList<ProcessThread> processThreads;
    
    // Semáforo que protege el acceso a las colas (cuando varios hilos quieran
    // modificar las colas al mismo tiempo, este semáforo asegura que solo uno lo haga a la vez)
    private final Semaphore queueLock;
    
    // Para generar números aleatorios
    private final Random random;
    
    // Una fábrica que crea objetos de planificación
    private final PolicyFactory policyFactory;
    
    // El algoritmo de planificación actual (FCFS, RR,...)
    private volatile SchedulingPolicy policy;
    
    // El tipo de política seleccionada (enum)
    private volatile PolicyType policyType;
    
    //Cuántos ciclos máximos puede ejecutarse un proceso antes de ser desalojado
    private volatile int quantum;
    
    // Cuántos ciclos le quedan al proceso actual antes de que se acabe su quantum
    private volatile int quantumRemaining;
    
    // El reloj global del sistema (se incrementa en cada ciclo)
    private int clockTick;
    
    // La duración de cada ciclo en milisegundos (input del usuario)
    private volatile int cycleMs;
    
    // Cuántos procesos pueden estar en memoria principal simultáneamente
    // Esto activa el planificador de mediano plazo (suspender procesos cuando se llena)
    private volatile int maxInMemory;
    
    // Cuántos ciclos le quedan al cambio de contexto.
    // El cambio de contexto no es instantáneo - toma tiempo (1 ciclo aquí).
    private volatile int contextSwitchRemaining;
    
    // El proceso que está actualmente en EJECUCIÓN (usando la CPU)
    private volatile Pcb running;
    
    // El hilo del reloj. Este hilo ejecuta los tick() periódicamente.
    private ClockThread clockThread;
    private volatile EngineListener listener;
    private volatile boolean paused;
    private volatile boolean interruptActive;
    private volatile int interruptRemaining;
    private int cpuBusyCycles;
    private int totalCycles;
    private int completedCount;
    private int successCount;
    private int totalWaitTime;
    private int processId;

    public SimulatorEngine() {
        this.newQueue = new SimpleQueue<>();
        this.readyQueue = new SimpleQueue<>();
        this.blockedQueue = new SimpleQueue<>();
        this.terminatedQueue = new SimpleQueue<>();
        this.readySuspendedQueue = new SimpleQueue<>();
        this.blockedSuspendedQueue = new SimpleQueue<>();
        this.processThreads = new SimpleLinkedList<>();
        this.queueLock = new Semaphore(1);
        this.random = new Random();
        this.policyFactory = new PolicyFactory();
        this.policyType = PolicyType.FCFS;
        this.quantum = 3;
        this.quantumRemaining = this.quantum;
        this.policy = policyFactory.create(policyType, quantum);
        this.clockTick = 0;
        this.cycleMs = 300;
        this.maxInMemory = 12;
        this.contextSwitchRemaining = 0;
        this.paused = true;
        this.interruptActive = false;
        this.interruptRemaining = 0;
        this.cpuBusyCycles = 0;
        this.totalCycles = 0;
        this.completedCount = 0;
        this.successCount = 0;
        this.totalWaitTime = 0;
        this.processId = 1;
    }

    public void setListener(EngineListener listener) {
        this.listener = listener;
    }

    public int getCycleMs() {
        return cycleMs;
    }

    public void setCycleMs(int cycleMs) {
        this.cycleMs = Math.max(1, cycleMs);
    }

    public void setMaxInMemory(int maxInMemory) {
        this.maxInMemory = Math.max(1, maxInMemory);
    }

    public void setPolicyType(PolicyType policyType) {
        this.policyType = policyType;
        this.policy = policyFactory.create(policyType, quantum);
        if (policy.isRoundRobin()) {
            this.quantumRemaining = policy.getQuantum();
        }
        log("Policy changed to " + policyType);
    }

    public PolicyType getPolicyType() {
        return policyType;
    }

    public void setQuantum(int quantum) {
        this.quantum = Math.max(1, quantum);
        this.quantumRemaining = this.quantum;
        if (policyType == PolicyType.RR) {
            this.policy = policyFactory.create(policyType, this.quantum);
        }
    }

    public void start() {
        if (clockThread != null) {
            return;
        }
        paused = false;
        clockThread = new ClockThread(this);
        clockThread.start();
        if (readyQueue.isEmpty() && newQueue.isEmpty()) {
            generateInitialProcesses(8);
        }
        log("Simulation started");
    }

    public void pause() {
        paused = true;
        log("Simulation paused");
    }

    public void resume() {
        paused = false;
        log("Simulation resumed");
    }

    public void stop() {
        paused = true;
        if (clockThread != null) {
            clockThread.requestStop();
            clockThread = null;
        }
        stopProcessThreads();
        log("Simulation stopped");
    }

    private void stopProcessThreads() {
        for (int i = 0; i < processThreads.size(); i += 1) {
            ProcessThread thread = processThreads.get(i);
            if (thread != null) {
                thread.requestStop();
            }
        }
        processThreads.clear();
    }

    public void tick() {
        if (paused) {
            return;
        }
        totalCycles += 1;
        clockTick += 1;
        updateDeadlines();
        refillMemory();

        boolean osBusy = interruptActive || contextSwitchRemaining > 0;

        // Maneja las interrupciones
        if (interruptActive) {
            interruptRemaining -= 1;
            if (interruptRemaining <= 0) {
                interruptActive = false;
                log("Interrupt finished");
            }
        } else if (contextSwitchRemaining > 0) { // Si no hay interrupción pero hay un cambio de contexto en progreso
            contextSwitchRemaining -= 1; // Lo continúa
        } else { //Si no hay interrupciones ni cambios de contexto, y no hay proceso ejecutándose
            if (running == null) {
                scheduleNext(); // Llama al planificador de corto plazo
                if (contextSwitchRemaining > 0) {
                    notifySnapshot(true); // Si eso inició un cambio de contexto, notifica a la GUI y termina el tick
                    return;
                }
            }
            if (running != null) { //  Si hay un proceso para ejecutar, ejecuta UN ciclo de instrucción de ese proceso
                runCurrentProcess();
                osBusy = false;
            }
        }

        if (!osBusy && running != null) {
            cpuBusyCycles += 1;
        }

        notifySnapshot(osBusy);
    }

    private void updateDeadlines() {
        boolean locked = false;
        try {
            queueLock.acquire();
            locked = true;
            updateQueueDeadlines(newQueue);
            updateQueueDeadlines(readyQueue);
            updateQueueDeadlines(blockedQueue);
            updateQueueDeadlines(readySuspendedQueue);
            updateQueueDeadlines(blockedSuspendedQueue);
            if (running != null) {
                running.updateDeadlineRemaining();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            if (locked) {
                queueLock.release();
            }
        }
    }

    private void updateQueueDeadlines(SimpleQueue<Pcb> queue) {
        for (int i = 0; i < queue.size(); i += 1) {
            Pcb pcb = queue.get(i);
            if (pcb != null) {
                pcb.updateDeadlineRemaining();
            }
        }
    }

    private void refillMemory() {
        boolean locked = false;
        try {
            queueLock.acquire();
            locked = true;
            int inMemory = readyQueue.size() + blockedQueue.size() + (running != null ? 1 : 0);
            while (!readySuspendedQueue.isEmpty() && inMemory < maxInMemory) {
                Pcb pcb = readySuspendedQueue.dequeue();
                if (pcb != null) {
                    pcb.setState(ProcessState.READY);
                    pcb.markReady(clockTick);
                    readyQueue.enqueue(pcb);
                    inMemory += 1;
                    log("Process " + pcb.getId() + " moved to READY from suspend");
                }
            }
            while (!newQueue.isEmpty() && inMemory < maxInMemory) {
                Pcb pcb = newQueue.dequeue();
                if (pcb != null) {
                    pcb.setState(ProcessState.READY);
                    pcb.markReady(clockTick);
                    readyQueue.enqueue(pcb);
                    inMemory += 1;
                }
            }
            while (!newQueue.isEmpty() && inMemory >= maxInMemory) {
                Pcb pcb = newQueue.dequeue();
                if (pcb != null) {
                    pcb.setState(ProcessState.READY_SUSPENDED);
                    readySuspendedQueue.enqueue(pcb);
                    log("Proceso " + pcb.getId() + " movido a Suspendido (READY_SUSPENDED)");
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            if (locked) {
                queueLock.release();
            }
        }
    }

    private void scheduleNext() {
        boolean locked = false;
        try {
            queueLock.acquire();
            locked = true;
            int index = policy.pickNextIndex(readyQueue, clockTick);
            if (index >= 0) {
                Pcb next = readyQueue.removeAt(index);
                if (next != null) {
                    running = next;
                    running.setState(ProcessState.RUNNING);
                    running.markRunning(clockTick);
                    contextSwitchRemaining = 1;
                    if (policy.isRoundRobin()) {
                        quantumRemaining = policy.getQuantum();
                    }
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            if (locked) {
                queueLock.release();
            }
        }
    }

    private void runCurrentProcess() {
        ProcessThread thread = findProcessThread(running);
        if (thread == null) {
            thread = new ProcessThread(running);
            processThreads.addLast(thread);
            thread.start();
        }

        try {
            thread.runOneCycle();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return;
        }

        CycleEvent event = running.getLastEvent();
        if (event == CycleEvent.IO_BLOCK) {
            handleIoBlock(running);
            running = null;
            contextSwitchRemaining = 1;
            return;
        }
        if (event == CycleEvent.FINISHED) {
            handleFinish(running);
            running = null;
            contextSwitchRemaining = 1;
            return;
        }

        if (policy.isRoundRobin()) {
            quantumRemaining -= 1;
            if (quantumRemaining <= 0) {
                moveRunningToReady();
                quantumRemaining = policy.getQuantum();
                return;
            }
        }

        if (policy.isPreemptive()) {
            boolean preempt;
            boolean locked = false;
            try {
                queueLock.acquire();
                locked = true;
                preempt = policy.shouldPreempt(running, readyQueue, clockTick);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            } finally {
                if (locked) {
                    queueLock.release();
                }
            }
            if (preempt) {
                moveRunningToReady();
            }
        }
    }

    private void moveRunningToReady() {
        if (running == null) {
            return;
        }
        boolean locked = false;
        try {
            queueLock.acquire();
            locked = true;
            running.setState(ProcessState.READY);
            running.markReady(clockTick);
            readyQueue.enqueue(running);
            running = null;
            contextSwitchRemaining = 1;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            if (locked) {
                queueLock.release();
            }
        }
    }

    private void handleIoBlock(Pcb pcb) {
        boolean locked = false;
        try {
            queueLock.acquire();
            locked = true;
            int inMemory = readyQueue.size() + blockedQueue.size() + (running != null ? 1 : 0);
            if (inMemory >= maxInMemory) {
                pcb.setState(ProcessState.BLOCKED_SUSPENDED);
                blockedSuspendedQueue.enqueue(pcb);
                log("Proceso " + pcb.getId() + " movido a Suspendido (BLOCKED_SUSPENDED)");
            } else {
                pcb.setState(ProcessState.BLOCKED);
                blockedQueue.enqueue(pcb);
                log("Process " + pcb.getId() + " blocked by IO");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            if (locked) {
                queueLock.release();
            }
        }
        int ioCycles = 2 + random.nextInt(5);
        IoServiceThread ioThread = new IoServiceThread(this, pcb, ioCycles, cycleMs);
        ioThread.start();
    }

    private void handleFinish(Pcb pcb) {
        boolean locked = false;
        try {
            queueLock.acquire();
            locked = true;
            pcb.setState(ProcessState.TERMINATED);
            pcb.markFinished(clockTick);
            terminatedQueue.enqueue(pcb);
            completedCount += 1;
            totalWaitTime += pcb.getTotalWaitTime();
            if (pcb.metDeadline()) {
                successCount += 1;
            } else {
                log("Fallo de Deadline en Proceso " + pcb.getId());
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            if (locked) {
                queueLock.release();
            }
        }
        log("Process " + pcb.getId() + " finished");
    }

    private ProcessThread findProcessThread(Pcb pcb) {
        for (int i = 0; i < processThreads.size(); i += 1) {
            ProcessThread thread = processThreads.get(i);
            if (thread != null && thread.getName().equals("Process-" + pcb.getId())) {
                return thread;
            }
        }
        return null;
    }

    public void finishIo(Pcb pcb) {
        boolean locked = false;
        try {
            queueLock.acquire();
            locked = true;
            blockedQueue.remove(pcb);
            blockedSuspendedQueue.remove(pcb);
            int inMemory = readyQueue.size() + blockedQueue.size() + (running != null ? 1 : 0);
            if (inMemory < maxInMemory) {
                pcb.setState(ProcessState.READY);
                pcb.markReady(clockTick);
                readyQueue.enqueue(pcb);
            } else {
                pcb.setState(ProcessState.READY_SUSPENDED);
                readySuspendedQueue.enqueue(pcb);
                log("Proceso " + pcb.getId() + " movido a Suspendido (READY_SUSPENDED)");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            if (locked) {
                queueLock.release();
            }
        }
        log("IO completed for process " + pcb.getId());
    }

    public void startInterrupt(String message, int cycles) {
        interruptActive = true;
        interruptRemaining = Math.max(1, cycles);
        log("Interrupt detected: " + message);
    }

    public void endInterrupt() {
        interruptActive = false;
    }

    public void triggerInterrupt(String message) {
        int interruptCycles = 2 + random.nextInt(6);
        InterruptHandler handler = new InterruptHandler(this, message, interruptCycles, cycleMs);
        handler.start();
    }

    public void addProcess(Pcb pcb) {
        boolean locked = false;
        try {
            queueLock.acquire();
            locked = true;
            newQueue.enqueue(pcb);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            if (locked) {
                queueLock.release();
            }
        }
        log("Process " + pcb.getId() + " created");
    }

    public Pcb createRandomProcess() {
        int instructions = 15 + random.nextInt(66);
        int priority = 1 + random.nextInt(9);
        int period = 5 + random.nextInt(20);
        double deadlineFactor = 3.0 + (random.nextDouble() * 2.0);
        int deadlineOffset = 20 + random.nextInt(41);
        int deadline = (int) Math.round(instructions * deadlineFactor) + deadlineOffset;
        int ioEvery = random.nextInt(4) == 0 ? 3 + random.nextInt(6) : 0;
        int ioService = ioEvery > 0 ? 2 + random.nextInt(5) : 0;
        int id = nextProcessId();
        Pcb pcb = new Pcb(id, "P" + id, instructions, priority,
                period, deadline, ioEvery, ioService, clockTick);
        return pcb;
    }

    public void generateInitialProcesses(int count) {
        for (int i = 0; i < count; i += 1) {
            addProcess(createRandomProcess());
        }
    }

    public void generateManyProcesses(int count) {
        for (int i = 0; i < count; i += 1) {
            addProcess(createRandomProcess());
        }
        log("Generated " + count + " random processes");
    }

    public int getNextProcessId() {
        return processId;
    }

    public void advanceProcessId(int count) {
        if (count > 0) {
            processId += count;
        }
    }

    private int nextProcessId() {
        int id = processId;
        processId += 1;
        return id;
    }

    private void notifySnapshot(boolean osBusy) {
        if (listener == null) {
            return;
        }
        listener.onTick(buildSnapshot(osBusy));
    }

    private EngineSnapshot buildSnapshot(boolean osBusy) {
        boolean locked = false;
        try {
            queueLock.acquire();
            locked = true;
            Pcb[] runningArray = running == null ? new Pcb[0] : new Pcb[] { running };
            return new EngineSnapshot(clockTick, osBusy, policyType,
                    computeCpuUtilization(), computeMissionSuccessRate(), computeThroughput(),
                    computeAverageWaitTime(), toArray(newQueue), toArray(readyQueue),
                    toArray(blockedQueue), toArray(readySuspendedQueue),
                    toArray(blockedSuspendedQueue), toArray(terminatedQueue), runningArray);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return new EngineSnapshot(clockTick, osBusy, policyType,
                    computeCpuUtilization(), computeMissionSuccessRate(), computeThroughput(),
                    computeAverageWaitTime(), new Pcb[0], new Pcb[0], new Pcb[0],
                    new Pcb[0], new Pcb[0], new Pcb[0], new Pcb[0]);
        } finally {
            if (locked) {
                queueLock.release();
            }
        }
    }

    private double computeCpuUtilization() {
        if (totalCycles == 0) {
            return 0.0;
        }
        return (double) cpuBusyCycles / (double) totalCycles;
    }

    private double computeMissionSuccessRate() {
        if (completedCount == 0) {
            return 0.0;
        }
        return (double) successCount / (double) completedCount;
    }

    private double computeThroughput() {
        if (clockTick == 0) {
            return 0.0;
        }
        return (double) completedCount / (double) clockTick;
    }

    private double computeAverageWaitTime() {
        if (completedCount == 0) {
            return 0.0;
        }
        return (double) totalWaitTime / (double) completedCount;
    }

    private Pcb[] toArray(SimpleQueue<Pcb> queue) {
        Pcb[] items = new Pcb[queue.size()];
        for (int i = 0; i < queue.size(); i += 1) {
            items[i] = queue.get(i);
        }
        return items;
    }

    private void log(String message) {
        if (listener != null) {
            listener.onLog("[" + clockTick + "] " + message);
        }
    }
}
