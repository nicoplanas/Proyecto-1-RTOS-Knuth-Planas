package com.rtosim.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class MainFrame extends JFrame {
	public MainFrame() {
		setTitle("RTOS Simulator");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(960, 640));
		setSize(1200, 760);
		setLocationRelativeTo(null);

		GradientPanel root = new GradientPanel();
		root.setLayout(new BorderLayout());

		JLabel title = new JLabel("RTOS Simulator", SwingConstants.CENTER);
		title.setFont(Theme.TITLE_FONT);
		title.setForeground(Theme.TEXT_PRIMARY);
		title.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 12, 18, 12));

		JPanel center = new JPanel(new BorderLayout());
		center.setOpaque(false);
		center.add(title, BorderLayout.CENTER);

		root.add(center, BorderLayout.CENTER);
		setContentPane(root);
	}
}
