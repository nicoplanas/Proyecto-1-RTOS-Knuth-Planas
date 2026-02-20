package com.rtosim.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TitledPanel extends JPanel {
    private final JLabel titleLabel;

    public TitledPanel(String title) {
        setOpaque(false);
        setLayout(new java.awt.BorderLayout());
        titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.HEADER_FONT);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 4, 12));
        add(titleLabel, java.awt.BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
    }

    public JPanel getBodyPanel() {
        JPanel body = new JPanel(new java.awt.BorderLayout());
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(4, 10, 10, 10));
        add(body, java.awt.BorderLayout.CENTER);
        return body;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension base = super.getPreferredSize();
        if (base == null) {
            return new Dimension(240, 180);
        }
        return base;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        g2.setColor(new Color(0, 0, 0, 65));
        g2.fillRoundRect(7, 9, w - 14, h - 14, 18, 18);

        g2.setColor(Theme.PANEL_BG);
        g2.fillRoundRect(4, 4, w - 10, h - 10, 16, 16);

        g2.setColor(Theme.PANEL_BORDER);
        g2.drawRoundRect(4, 4, w - 10, h - 10, 16, 16);

        g2.setColor(new Color(Theme.PANEL_GLOW.getRed(), Theme.PANEL_GLOW.getGreen(),
            Theme.PANEL_GLOW.getBlue(), 48));
        g2.drawRoundRect(5, 5, w - 12, h - 12, 14, 14);

        g2.dispose();
        super.paintComponent(g);
    }
}
