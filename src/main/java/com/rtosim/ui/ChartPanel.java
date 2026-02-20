package com.rtosim.ui;

import java.awt.Dimension;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class ChartPanel extends org.jfree.chart.ChartPanel {
    private final XYSeries cpuSeries;
    private final XYSeries successSeries;
    private int timeIndex;

    public ChartPanel() {
        super(buildChart());
        XYPlot plot = getChart().getXYPlot();
        XYSeriesCollection dataset = (XYSeriesCollection) plot.getDataset();

        this.cpuSeries = dataset.getSeries(0);
        this.successSeries = dataset.getSeries(1);
        this.timeIndex = 0;

        setPreferredSize(new Dimension(520, 360));
        setBackground(Theme.PANEL_BG.darker());
        setMouseWheelEnabled(false);
        setDomainZoomable(false);
        setRangeZoomable(false);
        setMinimumDrawWidth(1);
        setMinimumDrawHeight(1);
        setMaximumDrawWidth(Integer.MAX_VALUE);
        setMaximumDrawHeight(Integer.MAX_VALUE);
    }

    public void addPoint(double cpuUtilization, double missionSuccessRate) {
        timeIndex += 1;
        cpuSeries.add(timeIndex, clamp01(cpuUtilization));
        successSeries.add(timeIndex, clamp01(missionSuccessRate));

        int maxPoints = 220;
        if (cpuSeries.getItemCount() > maxPoints) {
            cpuSeries.remove(0);
        }
        if (successSeries.getItemCount() > maxPoints) {
            successSeries.remove(0);
        }
    }

    public void reset() {
        cpuSeries.clear();
        successSeries.clear();
        timeIndex = 0;
    }

    private static JFreeChart buildChart() {
        XYSeries cpu = new XYSeries("CPU");
        XYSeries success = new XYSeries("Mission Success");

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(cpu);
        dataset.addSeries(success);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Performance Over Time",
                "Time",
                "Rate",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);

        chart.setBackgroundPaint(Theme.PANEL_BG);
        chart.getTitle().setPaint(Theme.TEXT_PRIMARY);
        chart.getTitle().setFont(Theme.HEADER_FONT);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Theme.TABLE_BG);
        plot.setDomainGridlinePaint(new java.awt.Color(255, 255, 255, 24));
        plot.setRangeGridlinePaint(new java.awt.Color(255, 255, 255, 24));
        plot.getDomainAxis().setLabelPaint(Theme.TEXT_MUTED);
        plot.getDomainAxis().setTickLabelPaint(Theme.TEXT_MUTED);
        plot.getRangeAxis().setLabelPaint(Theme.TEXT_MUTED);
        plot.getRangeAxis().setTickLabelPaint(Theme.TEXT_MUTED);
        // Allow a small visual padding so lines don't look vertically squashed
        // Disable auto-range and force a slight padding around [0,1]
        plot.getRangeAxis().setAutoRange(false);
        plot.getRangeAxis().setRange(-0.05, 1.05);

        // Ensure minimum size so layout doesn't squash the chart too much
        plot.getRenderer().setDefaultStroke(new java.awt.BasicStroke(2.0f));

        plot.getRenderer().setSeriesPaint(0, Theme.ACCENT);
        plot.getRenderer().setSeriesPaint(1, Theme.PANEL_GLOW);

        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(Theme.PANEL_BG);
            chart.getLegend().setItemPaint(Theme.TEXT_PRIMARY);
        }

        return chart;
    }

    private double clamp01(double value) {
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 1.0) {
            return 1.0;
        }
        return value;
    }
}
