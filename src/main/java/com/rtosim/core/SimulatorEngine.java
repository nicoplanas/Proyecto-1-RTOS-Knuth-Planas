package com.rtosim.core; // Definimos el paquete donde vive esta clase

// Importamos las clases necesarias de otros paquetes
import com.rtosim.model.CycleEvent; // Lo que un proceso devuelve después de ejecutar una instrucción
import com.rtosim.model.Pcb; // El Process Control Block - la estructura de datos que representa un proceso
import com.rtosim.model.ProcessState; // Un enum con los estados posibles (NUEVO, LISTO, EJECUCIÓN,...)

// Las clases para implementar los algoritmos de planificación
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
        this.quantum = 3;
        this.quantumRemaining = this.quantum;
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
        
    }
    