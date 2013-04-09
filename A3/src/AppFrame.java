package com.sketch;

import java.awt.BorderLayout;

import javax.swing.SwingUtilities;
import javax.swing.JFrame;

public class AppFrame extends JFrame implements ToolbarListener, CanvasListener, TimeListener {

	private ToolbarPanel toolbar;
	private CanvasView canvas;
	private TimePanel timePanel;

	private DrawMode mode;

	public AppFrame() {
		super();

		canvas = new CanvasView(this);
		toolbar = new ToolbarPanel(this);
		timePanel = new TimePanel(this);

		canvas.setTotalFrames(timePanel.getTotalFrames());

		this.getContentPane().add(toolbar, BorderLayout.NORTH);
		this.getContentPane().add(canvas, BorderLayout.CENTER);
		this.getContentPane().add(timePanel, BorderLayout.SOUTH);

		this.pack();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void setDrawMode(DrawMode m) {
		mode = m;
		canvas.setDrawMode(m);
	}

	public void startPlaying() {}
	public void stopPlaying() {}

	public void changeFrame(int frame) {
		canvas.changeFrame(frame);
	}

	public void addFrames(int frames) {
		canvas.addFrames(frames);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				AppFrame frame = new AppFrame();
				frame.setVisible(true);
			}
		});
	}
}