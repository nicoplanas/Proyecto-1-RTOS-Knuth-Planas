package com.rtosim.core;

public interface EngineListener {
    void onTick(EngineSnapshot snapshot);

    void onLog(String message);
}
