package com.rtosim.ui;

import com.rtosim.model.Pcb;
import javax.swing.table.AbstractTableModel;

public class PcbTableModel extends AbstractTableModel {
    public static final int COL_ID = 0;
    public static final int COL_NAME = 1;
    public static final int COL_STATE = 2;
    public static final int COL_PC = 3;
    public static final int COL_MAR = 4;
    public static final int COL_PRIORITY = 5;
    public static final int COL_DEADLINE = 6;
    public static final int COL_REMAINING = 7;
    public static final int COL_PROCESS = 8;
    public static final int COL_TOTAL_INSTRUCTIONS = 9;

    private final String[] columns;
    private final int[] mapping;
    private Pcb[] data;

    public PcbTableModel() {
        this(new String[] {
            "ID", "Name", "State", "PC", "MAR", "Priority", "Deadline", "Remaining"
        }, new int[] {
            COL_ID, COL_NAME, COL_STATE, COL_PC, COL_MAR, COL_PRIORITY, COL_DEADLINE, COL_REMAINING
        });
    }

    public PcbTableModel(String[] columns, int[] mapping) {
        if (columns == null || mapping == null || columns.length != mapping.length) {
            throw new IllegalArgumentException("Invalid column mapping");
        }
        this.columns = columns;
        this.mapping = mapping;
        this.data = new Pcb[0];
    }

    public void setData(Pcb[] data) {
        this.data = data == null ? new Pcb[0] : data;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return data.length;
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= data.length) {
            return "";
        }
        Pcb pcb = data[rowIndex];
        if (pcb == null) {
            return "";
        }
        int col = mapping[columnIndex];
        return switch (col) {
            case COL_ID -> pcb.getId();
            case COL_NAME -> pcb.getName();
            case COL_STATE -> pcb.getState();
            case COL_PC -> pcb.getPc();
            case COL_MAR -> pcb.getMar();
            case COL_PRIORITY -> pcb.getPriority();
            case COL_DEADLINE -> Math.max(0, pcb.getDeadlineRemaining());
            case COL_REMAINING -> pcb.getRemainingInstructions();
            case COL_PROCESS -> pcb.getName() + " [ID:" + pcb.getId() + "]";
            case COL_TOTAL_INSTRUCTIONS -> pcb.getTotalInstructions();
            default -> "";
        };
    }
}
