package com.rtosim.ui;

import com.rtosim.core.EngineListener;
import com.rtosim.core.EngineSnapshot;
import com.rtosim.core.SimulatorEngine;
import com.rtosim.io.CsvProcessLoader;
import com.rtosim.io.JsonProcessLoader;
import com.rtosim.io.ProcessLoader;
import com.rtosim.model.Pcb;
import com.rtosim.scheduling.PolicyType;
import com.rtosim.struct.SimpleQueue;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.BoxLayout;

public class MainFrame extends JFrame implements EngineListener {
    private final SimulatorEngine engine;
    private final ChartPanel missionChartPanel;
    private final ChartPanel memoryChartPanel;
    private final PieChartPanel missionPieChartPanel;
    private final PieChartPanel memoryPieChartPanel;
    private final JTextArea logArea;
    private final JLabel clockLabel;
    private final JLabel cpuLabel;
    private final JLabel successLabel;
    private final JLabel throughputLabel;
    private final JLabel waitLabel;
    private final JLabel modeLabel;
    private final JLabel headerTitleLabel;
    private final JComboBox<String> policyCombo;
    private final JTextField cycleField;
    private final JTextField quantumField;
    private final JTextField maxMemoryField;
    private final JProgressBar memoryBar;
    private final JProgressBar runningBar;
    private final JProgressBar runningInstructionsBar;
    private final JLabel runningLabel;
    private final JLabel runningDeadlineLabel;
    private final JLabel runningInstructionsLabel;
    private int maxMemory;

    private final PcbTableModel readyModel;
    private final PcbTableModel blockedModel;
    private final PcbTableModel readySuspendedModel;
    private final PcbTableModel blockedSuspendedModel;
    private final PcbTableModel terminatedModel;
    private final PcbTableModel newModel;

    public MainFrame() {
        super("UNIMET-Sat RTOS Simulator");
        engine = new SimulatorEngine();
        engine.setListener(this);

        missionChartPanel = new ChartPanel();
        memoryChartPanel = new ChartPanel();
        missionPieChartPanel = new PieChartPanel();
        memoryPieChartPanel = new PieChartPanel();
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(Theme.MONO_FONT);
        logArea.setBackground(Theme.TABLE_BG);
        logArea.setForeground(Theme.TEXT_PRIMARY);
        logArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        clockLabel = new JLabel("MISSION CLOCK: Cycle 0", SwingConstants.RIGHT);
        clockLabel.setFont(Theme.MONO_FONT);
        clockLabel.setForeground(Theme.ACCENT);

        cpuLabel = new JLabel("CPU Util: 0%");
        successLabel = new JLabel("Mission Success: 0%");
        throughputLabel = new JLabel("Throughput: 0");
        waitLabel = new JLabel("Avg Wait: 0");
        modeLabel = new JLabel("CPU: USER");
        modeLabel.setForeground(new Color(45, 130, 90));

        headerTitleLabel = new JLabel("UNIMET-Sat RTOS Simulator - Mission Control");
        headerTitleLabel.setFont(Theme.TITLE_FONT);
        headerTitleLabel.setForeground(Theme.TEXT_PRIMARY);

        policyCombo = new JComboBox<>(new String[] {
            "FCFS", "RR", "SRT", "STATIC_PRIORITY", "RMS", "EDF"
        });
        cycleField = new JTextField("300", 5);
        quantumField = new JTextField("3", 4);
        maxMemoryField = new JTextField("12", 4);
        maxMemory = 12;

        styleInput(policyCombo);
        styleInput(cycleField);
        styleInput(quantumField);
        styleInput(maxMemoryField);

        memoryBar = new JProgressBar(0, 100);
        memoryBar.setStringPainted(true);
        memoryBar.setForeground(Theme.BAR_FILL);
        memoryBar.setBackground(Theme.BAR_TRACK);
        memoryBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.INPUT_BORDER),
            BorderFactory.createEmptyBorder(2, 3, 2, 3)));

        runningBar = new JProgressBar(0, 100);
        runningBar.setStringPainted(false);
        runningBar.setForeground(Theme.BAR_FILL);
        runningBar.setBackground(Theme.BAR_TRACK);
        runningBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.INPUT_BORDER),
            BorderFactory.createEmptyBorder(2, 3, 2, 3)));

        runningInstructionsBar = new JProgressBar(0, 100);
        runningInstructionsBar.setStringPainted(false);
        runningInstructionsBar.setForeground(Theme.ACCENT);
        runningInstructionsBar.setBackground(Theme.BAR_TRACK);
        runningInstructionsBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.INPUT_BORDER),
            BorderFactory.createEmptyBorder(2, 3, 2, 3)));

        runningLabel = new JLabel("Idle", SwingConstants.CENTER);
        runningLabel.setFont(Theme.HEADER_FONT);
        runningLabel.setForeground(Theme.TEXT_PRIMARY);
        runningDeadlineLabel = new JLabel("Deadline in: --", SwingConstants.CENTER);
        runningDeadlineLabel.setFont(Theme.BODY_FONT);
        runningDeadlineLabel.setForeground(Theme.TEXT_MUTED);
        runningInstructionsLabel = new JLabel("Instructions: --", SwingConstants.CENTER);
        runningInstructionsLabel.setFont(Theme.BODY_FONT);
        runningInstructionsLabel.setForeground(Theme.TEXT_MUTED);

        String[] pcbColumns = new String[] { "ID", "Name", "Status", "PC", "MAR", "Priority", "Instr", "Deadline" };
        int[] pcbMapping = new int[] {
            PcbTableModel.COL_ID,
            PcbTableModel.COL_NAME,
            PcbTableModel.COL_STATE,
            PcbTableModel.COL_PC,
            PcbTableModel.COL_MAR,
            PcbTableModel.COL_PRIORITY,
            PcbTableModel.COL_TOTAL_INSTRUCTIONS,
            PcbTableModel.COL_DEADLINE
        };

        readyModel = new PcbTableModel(pcbColumns, pcbMapping);
        blockedModel = new PcbTableModel(pcbColumns, pcbMapping);
        readySuspendedModel = new PcbTableModel(pcbColumns, pcbMapping);
        blockedSuspendedModel = new PcbTableModel(pcbColumns, pcbMapping);
        terminatedModel = new PcbTableModel(pcbColumns, pcbMapping);
        newModel = new PcbTableModel(pcbColumns, pcbMapping);

        GradientPanel root = new GradientPanel();
        root.setLayout(new BorderLayout());
        setContentPane(root);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(1440, 900));
        setMinimumSize(new Dimension(1220, 780));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(10, 14, 6, 14));
        header.add(headerTitleLabel, BorderLayout.WEST);
        header.add(clockLabel, BorderLayout.EAST);

        JPanel controlStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        controlStrip.setBackground(Theme.CONTROL_BG);
        controlStrip.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.TABLE_GRID),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        controlStrip.add(styledLabel("Policy"));
        controlStrip.add(policyCombo);
        controlStrip.add(styledLabel("Cycle (ms)"));
        controlStrip.add(cycleField);
        controlStrip.add(styledLabel("Quantum"));
        controlStrip.add(quantumField);
        controlStrip.add(styledLabel("Max Memory"));
        controlStrip.add(maxMemoryField);

        JButton applyButton = styledButton("Apply");
        applyButton.addActionListener(e -> applySettings());
        controlStrip.add(applyButton);

        JPanel actionStrip = new JPanel(new GridLayout(1, 9, 8, 6));
        actionStrip.setBackground(Theme.CONTROL_BG);
        actionStrip.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.TABLE_GRID),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        JButton startButton = styledButton("Start");
        startButton.addActionListener(e -> engine.start());
        JButton pauseButton = styledButton("Pause");
        pauseButton.addActionListener(e -> engine.pause());
        JButton resumeButton = styledButton("Resume");
        resumeButton.addActionListener(e -> engine.resume());
        JButton stopButton = styledButton("Stop");
        stopButton.addActionListener(e -> engine.stop());

        JButton generate20Button = styledButton("Generar 20");
        generate20Button.addActionListener(e -> engine.generateManyProcesses(20));
        JButton addRandomButton = styledButton("Tarea Emergencia");
        addRandomButton.addActionListener(e -> engine.addProcess(engine.createRandomProcess()));

        JButton loadCsvButton = styledButton("Load CSV");
        loadCsvButton.addActionListener(e -> loadFromFile(new CsvProcessLoader()));
        JButton loadJsonButton = styledButton("Load JSON");
        loadJsonButton.addActionListener(e -> loadFromFile(new JsonProcessLoader()));

        JButton interruptButton = styledButton("Interrupción");
        interruptButton.addActionListener(e -> engine.triggerInterrupt("External event"));

        actionStrip.add(startButton);
        actionStrip.add(pauseButton);
        actionStrip.add(resumeButton);
        actionStrip.add(stopButton);
        actionStrip.add(generate20Button);
        actionStrip.add(addRandomButton);
        actionStrip.add(loadCsvButton);
        actionStrip.add(loadJsonButton);
        actionStrip.add(interruptButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(header, BorderLayout.NORTH);
        JPanel strips = new JPanel();
        strips.setLayout(new BoxLayout(strips, BoxLayout.Y_AXIS));
        strips.setOpaque(false);
        strips.add(controlStrip);
        strips.add(javax.swing.Box.createVerticalStrut(6));
        strips.add(actionStrip);
        wrapper.add(strips, BorderLayout.CENTER);

        policyCombo.addActionListener(e -> {
            PolicyType type = PolicyType.valueOf((String) policyCombo.getSelectedItem());
            engine.setPolicyType(type);
        });

        return wrapper;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Mission Control", buildMissionControlTab());
        tabs.addTab("Memory Management", buildMemoryTab());
        styleTabs(tabs);
        tabs.addChangeListener(e -> {
            int index = tabs.getSelectedIndex();
            if (index == 0) {
                headerTitleLabel.setText("UNIMET-Sat RTOS Simulator - Mission Control");
            } else {
                headerTitleLabel.setText("UNIMET-Sat RTOS Simulator - Memory Management & Swap");
            }
        });
        return tabs;
    }

    private JPanel buildMissionControlTab() {
        GradientPanel panel = new GradientPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        // Top section: Ready, Running, Blocked
        JPanel topMonitor = new JPanel(new GridLayout(1, 3, 12, 12));
        topMonitor.setOpaque(false);
        topMonitor.add(buildQueuePanel("Ready Queue", readyModel));
        topMonitor.add(buildRunningPanel());
        topMonitor.add(buildQueuePanel("Blocked Queue (I/O)", blockedModel));

        // Bottom section: Process Lists (Left) and Log (Right)
        JPanel processLists = new JPanel(new GridLayout(1, 2, 10, 0));
        processLists.setOpaque(false);
        processLists.add(buildQueuePanel("New Processes", newModel));
        processLists.add(buildQueuePanel("Terminated Processes", terminatedModel));

        JTabbedPane bottomTabs = new JTabbedPane();
        bottomTabs.addTab("Process Lists", processLists);
        bottomTabs.addTab("Metrics", buildChartPanel());
        styleTabs(bottomTabs);

        JPanel logPanel = buildLogPanel();
        
        // Horizontal split for bottom section
        JSplitPane bottomSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, bottomTabs, logPanel);
        bottomSplit.setResizeWeight(0.65);
        bottomSplit.setBorder(BorderFactory.createEmptyBorder());
        bottomSplit.setOpaque(false);
        bottomSplit.setDividerSize(4);
        SwingUtilities.invokeLater(() -> bottomSplit.setDividerLocation(0.65));

        // Vertical split: Top Monitor vs Bottom Info
        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topMonitor, bottomSplit);
        mainSplit.setResizeWeight(0.52);
        mainSplit.setBorder(BorderFactory.createEmptyBorder());
        mainSplit.setOpaque(false);
        mainSplit.setDividerSize(4);
        SwingUtilities.invokeLater(() -> mainSplit.setDividerLocation(0.52));

        panel.add(mainSplit, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(1280, 800));
        return panel;
    }

    private JPanel buildMemoryTab() {
        GradientPanel panel = new GradientPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JPanel topRow = new JPanel(new GridLayout(1, 3, 12, 12));
        topRow.setOpaque(false);
        topRow.add(buildQueuePanel("Ready Queue (RAM)", readyModel));
        topRow.add(buildMemoryPanel());
        topRow.add(buildQueuePanel("Blocked Queue (RAM)", blockedModel));

        JPanel swapRow = buildSwapPanel();

        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 12, 12));
        bottomRow.setOpaque(false);
        bottomRow.add(buildQueuePanel("Ready-Suspended", readySuspendedModel));
        bottomRow.add(buildQueuePanel("Blocked-Suspended", blockedSuspendedModel));

        JPanel rows = new JPanel(new GridLayout(3, 1, 10, 10));
        rows.setOpaque(false);
        rows.add(topRow);
        rows.add(swapRow);
        rows.add(bottomRow);

        panel.add(rows, BorderLayout.CENTER);
        panel.add(buildChartPanel(memoryChartPanel, memoryPieChartPanel), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildQueuePanel(String title, PcbTableModel model) {
        TitledPanel panel = new TitledPanel(title);
        JPanel body = panel.getBodyPanel();
        JTable table = buildTable(model);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Theme.TABLE_BG);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        body.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildRunningPanel() {
        TitledPanel panel = new TitledPanel("Running Process (CPU)");
        JPanel body = panel.getBodyPanel();

        JPanel info = new JPanel(new GridLayout(5, 1, 0, 6));
        info.setOpaque(false);
        info.add(runningLabel);
        info.add(runningBar);
        info.add(runningDeadlineLabel);
        info.add(runningInstructionsBar);
        info.add(runningInstructionsLabel);

        JButton emergencyButton = new JButton("<html><center>EMERGENCY INTERRUPTION<br>(MICRO-METEORITE)</center></html>");
        emergencyButton.setBackground(Theme.ALERT);
        emergencyButton.setForeground(Color.WHITE);
        emergencyButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        emergencyButton.setFocusPainted(false);
        emergencyButton.setOpaque(true);
        emergencyButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(130, 46, 46), 2),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        emergencyButton.setMargin(new Insets(10, 12, 10, 12));
        emergencyButton.addActionListener(e -> engine.triggerInterrupt("Micro-meteorite"));

        body.add(info, BorderLayout.CENTER);
        body.add(emergencyButton, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildMemoryPanel() {
        TitledPanel panel = new TitledPanel("Main Memory (RAM)");
        JPanel body = panel.getBodyPanel();
        memoryBar.setPreferredSize(new Dimension(220, 22));
        body.add(memoryBar, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildSwapPanel() {
        TitledPanel panel = new TitledPanel("Swap Space (Disk)");
        JPanel body = panel.getBodyPanel();
        body.setLayout(new GridLayout(2, 2, 10, 10));

        JLabel swapOutLeft = new JLabel("Swap Out (Memory Full)", SwingConstants.CENTER);
        JLabel swapInLeft = new JLabel("Swap In (Memory Available)", SwingConstants.CENTER);
        JLabel swapOutRight = new JLabel("Swap Out (Memory Full)", SwingConstants.CENTER);
        JLabel swapInRight = new JLabel("Swap In (Memory Available)", SwingConstants.CENTER);

        styleSwapLabel(swapOutLeft);
        styleSwapLabel(swapInLeft);
        styleSwapLabel(swapOutRight);
        styleSwapLabel(swapInRight);

        body.add(swapOutLeft);
        body.add(swapOutRight);
        body.add(swapInLeft);
        body.add(swapInRight);

        return panel;
    }

    private JPanel buildLogPanel() {
        TitledPanel panel = new TitledPanel("Event Log");
        JPanel body = panel.getBodyPanel();
        // Prevent TextArea from dictating parent size indefinitely
        logArea.setRows(10); 
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Theme.TABLE_BG);
        // Important: Stop the scroll pane from growing with content
        scroll.setPreferredSize(new Dimension(400, 150)); 
        body.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildChartPanel() {
        return buildChartPanel(missionChartPanel, missionPieChartPanel);
    }

    private JPanel buildChartPanel(ChartPanel panelSource, PieChartPanel pieSource) {
        TitledPanel panel = new TitledPanel("CPU Utilization & Metrics");
        JPanel body = panel.getBodyPanel();

        // Left: main line chart. Right: pie chart. Use JSplitPane so left gets more space.
        JPanel leftWrapper = new JPanel(new BorderLayout());
        leftWrapper.setOpaque(false);
        leftWrapper.add(panelSource, BorderLayout.CENTER);
        leftWrapper.setPreferredSize(new Dimension(760, 380));

        JPanel rightWrapper = new JPanel(new BorderLayout());
        rightWrapper.setOpaque(false);
        pieSource.setPreferredSize(new Dimension(340, 340));
        rightWrapper.add(pieSource, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftWrapper, rightWrapper);
        split.setResizeWeight(0.7);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setOpaque(false);
        split.setDividerSize(6);
        SwingUtilities.invokeLater(() -> split.setDividerLocation(0.7));

        body.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JTable buildTable(PcbTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(25);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.setFont(Theme.BODY_FONT);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setBackground(Theme.TABLE_BG);
        table.setFillsViewportHeight(true);
        table.setGridColor(Theme.TABLE_GRID);
        table.setSelectionBackground(new Color(44, 76, 120));
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        
        table.getTableHeader().setBackground(Theme.TABLE_HEADER_BG);
        table.getTableHeader().setForeground(Theme.TEXT_PRIMARY);
        table.getTableHeader().setFont(Theme.HEADER_FONT);
        table.getTableHeader().setOpaque(true);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.TABLE_GRID));
        
        return table;
    }

    private JPanel buildStatusBar() {
        JPanel status = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        status.setBackground(new Color(16, 22, 34, 230));
        status.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.TABLE_GRID));
        styleStatusLabel(cpuLabel);
        styleStatusLabel(successLabel);
        styleStatusLabel(throughputLabel);
        styleStatusLabel(waitLabel);
        styleStatusLabel(modeLabel);
        status.add(cpuLabel);
        status.add(successLabel);
        status.add(throughputLabel);
        status.add(waitLabel);
        status.add(modeLabel);
        return status;
    }

    private void styleStatusLabel(JLabel label) {
        label.setFont(Theme.BODY_FONT);
        label.setForeground(Theme.TEXT_PRIMARY);
    }

    private void styleSwapLabel(JLabel label) {
        label.setFont(Theme.BODY_FONT);
        label.setForeground(Theme.TEXT_PRIMARY);
        label.setOpaque(true);
        label.setBackground(Theme.INPUT_BG);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.INPUT_BORDER),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
    }

    private JButton styledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(Theme.BUTTON_BG);
        button.setForeground(Theme.TEXT_PRIMARY);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BUTTON_BORDER),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        button.setFocusPainted(false);
        button.setFont(Theme.BODY_FONT);
        button.setMargin(new Insets(7, 12, 7, 12));
        button.setOpaque(true);
        return button;
    }

    private JLabel styledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.BODY_FONT);
        label.setForeground(Theme.TEXT_PRIMARY);
        return label;
    }

    private void styleInput(JTextField field) {
        field.setBackground(Theme.INPUT_BG);
        field.setForeground(Theme.INPUT_TEXT);
        field.setCaretColor(Theme.INPUT_TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.INPUT_BORDER),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        field.setFont(Theme.BODY_FONT);
    }

    private void styleInput(JComboBox<String> combo) {
        combo.setBackground(Theme.INPUT_BG);
        combo.setForeground(Theme.INPUT_TEXT);
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.INPUT_BORDER),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)));
        combo.setFont(Theme.BODY_FONT);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.setBackground(isSelected ? Theme.TABLE_HEADER_BG : Theme.INPUT_BG);
                label.setForeground(Theme.INPUT_TEXT);
                return label;
            }
        });
    }

    private void styleTabs(JTabbedPane tabs) {
        tabs.setBackground(Theme.PANEL_BG);
        tabs.setForeground(Theme.TEXT_PRIMARY);
        tabs.setFont(Theme.BODY_FONT);
        tabs.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    private void applySettings() {
        try {
            int cycle = parsePositiveInt(cycleField, "Cycle (ms)", 1, 10000);
            int quantum = parsePositiveInt(quantumField, "Quantum", 1, 1000);
            int maxMem = parsePositiveInt(maxMemoryField, "Max Memory", 1, 1000);

            engine.setCycleMs(cycle);
            engine.setQuantum(quantum);
            engine.setMaxInMemory(maxMem);
            maxMemory = Math.max(1, maxMem);
            JOptionPane.showMessageDialog(this, "Settings applied correctly",
                    "Settings", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private int parsePositiveInt(JTextField field, String name, int min, int max) {
        String raw = field.getText().trim();
        if (raw.isEmpty()) {
            throw new NumberFormatException(name + " is required");
        }

        int value;
        try {
            value = Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            throw new NumberFormatException(name + " must be an integer");
        }

        if (value < min || value > max) {
            throw new NumberFormatException(name + " must be between " + min + " and " + max);
        }
        return value;
    }

    private void loadFromFile(ProcessLoader loader) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        try {
            SimpleQueue<Pcb> loaded = loader.load(file, engine.getNextProcessId(), 0);
            int count = loaded.size();
            for (int i = 0; i < loaded.size(); i += 1) {
                engine.addProcess(loaded.get(i));
            }
            engine.advanceProcessId(count);
            JOptionPane.showMessageDialog(this, "Loaded " + count + " processes",
                    "Loaded", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load: " + ex.getMessage(),
                    "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void onTick(EngineSnapshot snapshot) {
        SwingUtilities.invokeLater(() -> {
            clockLabel.setText("MISSION CLOCK: Cycle " + snapshot.getClockTick());
            cpuLabel.setText("CPU Util: " + formatPercent(snapshot.getCpuUtilization()));
            successLabel.setText("Mission Success: " + formatPercent(snapshot.getMissionSuccessRate()));
            throughputLabel.setText("Throughput: " + formatValue(snapshot.getThroughput()));
            waitLabel.setText("Avg Wait: " + formatValue(snapshot.getAverageWaitTime()));
            modeLabel.setText(snapshot.isOsBusy() ? "CPU: OS" : "CPU: USER");
            modeLabel.setForeground(snapshot.isOsBusy() ? Theme.ALERT : new Color(45, 130, 90));

            readyModel.setData(snapshot.getReadyQueue());
            blockedModel.setData(snapshot.getBlockedQueue());
            readySuspendedModel.setData(snapshot.getReadySuspendedQueue());
            blockedSuspendedModel.setData(snapshot.getBlockedSuspendedQueue());
            terminatedModel.setData(snapshot.getTerminatedQueue());
            newModel.setData(snapshot.getNewQueue());

            updateRunningPanel(snapshot);
            updateMemoryPanel(snapshot);

                missionChartPanel.addPoint(snapshot.getCpuUtilization(), snapshot.getMissionSuccessRate());
                memoryChartPanel.addPoint(snapshot.getCpuUtilization(), snapshot.getMissionSuccessRate());
                missionPieChartPanel.updateData(snapshot.getMissionSuccessRate());
                memoryPieChartPanel.updateData(snapshot.getMissionSuccessRate());
        });
    }

    private void updateRunningPanel(EngineSnapshot snapshot) {
        Pcb[] running = snapshot.getRunning();
        if (running.length == 0) {
            runningLabel.setText("Idle");
            runningDeadlineLabel.setText("Deadline in: --");
            runningBar.setMaximum(100);
            runningBar.setValue(0);
            runningInstructionsBar.setMaximum(100);
            runningInstructionsBar.setValue(0);
            runningInstructionsLabel.setText("Instructions: --");
            return;
        }
        Pcb pcb = running[0];
        runningLabel.setText(pcb.getName() + " [ID:" + pcb.getId() + "]");
        int deadlineRemaining = pcb.getDeadlineRemaining();
        if (deadlineRemaining >= 0) {
            runningDeadlineLabel.setText("Deadline in: " + deadlineRemaining + " cycles");
        } else {
            runningDeadlineLabel.setText("Deadline missed by: " + Math.abs(deadlineRemaining) + " cycles");
        }
        int max = Math.max(1, pcb.getDeadline());
        int remaining = Math.max(0, deadlineRemaining);
        runningBar.setMaximum(max);
        runningBar.setValue(remaining);

        int totalInstructions = Math.max(1, pcb.getTotalInstructions());
        int completedInstructions = Math.max(0, totalInstructions - pcb.getRemainingInstructions());
        runningInstructionsBar.setMaximum(totalInstructions);
        runningInstructionsBar.setValue(Math.min(totalInstructions, completedInstructions));
        runningInstructionsLabel.setText("Instructions: " + completedInstructions + " / " + totalInstructions);
    }

    private void updateMemoryPanel(EngineSnapshot snapshot) {
        int inMemory = snapshot.getReadyQueue().length + snapshot.getBlockedQueue().length
                + snapshot.getRunning().length;
        int pct = (int) Math.min(100, Math.round((inMemory * 100.0) / Math.max(1, maxMemory)));
        memoryBar.setValue(pct);
        memoryBar.setString(pct + "%");
    }

    @Override
    public void onLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private String formatPercent(double value) {
        return String.format("%.1f%%", value * 100.0);
    }

    private String formatValue(double value) {
        return String.format("%.2f", value);
    }
}
