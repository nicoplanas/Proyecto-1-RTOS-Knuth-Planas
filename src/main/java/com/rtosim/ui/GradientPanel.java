package com.rtosim.ui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class GradientPanel extends JPanel {
    public GradientPanel() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        GradientPaint paint = new GradientPaint(0, 0, Theme.BG_TOP, 0, h, Theme.BG_BOTTOM);
        g2.setPaint(paint);
        g2.fillRect(0, 0, w, h);

        g2.setColor(new Color(255, 255, 255, 10));
        for (int y = 16; y < h; y += 110) {
            for (int x = 24; x < w; x += 140) {
                g2.fillOval(x, y, 3, 3);
            }
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
