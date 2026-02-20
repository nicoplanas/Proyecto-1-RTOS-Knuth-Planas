package com.rtosim.io;

import com.rtosim.model.Pcb;
import com.rtosim.struct.SimpleQueue;
import java.io.File;

public interface ProcessLoader {
    SimpleQueue<Pcb> load(File file, int startId, int clockTick) throws Exception;
}
