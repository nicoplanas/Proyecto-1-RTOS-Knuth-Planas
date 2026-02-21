package com.rtosim.ui;

import java.awt.Dimension;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

public class PieChartPanel extends org.jfree.chart.ChartPanel {
    private final DefaultPieDataset dataset;

    public PieChartPanel() {
        super(buildChart());
        PiePlot plot = (PiePlot) getChart().getPlot();
        this.dataset = (DefaultPieDataset) plot.getDataset();
        setPreferredSize(new Dimension(280, 280));
        setBackground(Theme.PANEL_BG.darker());
        setMouseWheelEnabled(false);
        setDomainZoomable(false);
        setRangeZoomable(false);
        setMinimumDrawWidth(1);
        setMinimumDrawHeight(1);
        setMaximumDrawWidth(Integer.MAX_VALUE);
        setMaximumDrawHeight(Integer.MAX_VALUE);
    }

    private static JFreeChart buildChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Success", 0.0);
        dataset.setValue("Failure", 1.0);

        JFreeChart chart = ChartFactory.createPieChart(
                "Mission Success Ratio",
                dataset,
                true,
                false,
                false);

        chart.setBackgroundPaint(Theme.PANEL_BG);
        chart.getTitle().setPaint(Theme.TEXT_PRIMARY);
        chart.getTitle().setFont(Theme.HEADER_FONT);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Theme.TABLE_BG);
        plot.setOutlineVisible(false);
        plot.setSectionPaint("Success", Theme.ACCENT);
        plot.setSectionPaint("Failure", Theme.PANEL_GLOW);
        plot.setLabelPaint(Theme.TEXT_MUTED);
        plot.setSimpleLabels(true);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {2}"));

        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(Theme.PANEL_BG);
            chart.getLegend().setItemPaint(Theme.TEXT_PRIMARY);
        }

        return chart;
    }

    public void updateData(double successRate) {
        double success = Math.max(0.0, Math.min(1.0, successRate));
        double failure = 1.0 - success;
        dataset.setValue("Success", success);
        dataset.setValue("Failure", failure);
    }
}
