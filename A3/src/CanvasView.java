package com.sketch;

import java.util.ArrayList;

import javax.swing.JPanel;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Polygon;
import java.awt.Point;
import java.awt.BasicStroke;

import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputListener;

public class CanvasView extends JPanel implements MouseInputListener {
	
	private CanvasListener delegate;

	private CanvasModel model;

	private DrawMode mode;

	private int currentFrame;

	private boolean mouseDown;

	private LineSegment drawBuffer;

	private Polygon selectionBox;

	private Point mouseStart;
	private Point mouseEnd;

	private ArrayList<LineSegment> selectedSegments;

	private BasicStroke drawStroke;
	private BasicStroke selectStroke;
	private BasicStroke eraseStroke;

	public CanvasView(CanvasListener delegate) {
		super();

		this.delegate = delegate;

		drawStroke = new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		float[] dashPattern = {1.0f, 1.0f};
		selectStroke = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, dashPattern, 2.0f);
		eraseStroke = new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);

		drawBuffer = null;
		selectionBox = null;
		currentFrame = 0;

		mouseDown = false;
		addMouseListener(this);
		addMouseMotionListener(this);

		model = new CanvasModel();

		this.setPreferredSize(new Dimension(800, 600));
	}

	public void setDrawMode(DrawMode m) {
		mode = m;
	}

	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);

		Graphics2D g = (Graphics2D)graphics.create();

		g.setColor(Color.WHITE);

		g.fill(this.getBounds());

		g.setColor(Color.BLACK);
		g.setStroke(drawStroke);
		
		model.drawLines(g, currentFrame);

		if (drawBuffer != null)
			drawBuffer.paint(g);
		if (mouseDown && mode == DrawMode.ERASE) {
			g.setColor(Color.RED);
			g.setStroke(eraseStroke);
			g.drawLine((int)mouseStart.getX(), (int)mouseStart.getY(), (int)mouseEnd.getX(), (int)mouseEnd.getY());
		}
	}

	public void changeFrame(int frame) {
		this.currentFrame = frame;

		if (selected() && mouseDown) {
			Delta d = new Delta(mouseStart, mouseEnd);
			if (selectedSegments != null) {
				for (LineSegment l : selectedSegments) {
					model.storeFrame(l, d, currentFrame);
				}
			}
			mouseStart = new Point(mouseEnd);
		}

		this.repaint(this.getBounds());
	}

	public void setTotalFrames(int frames) {
		model.setTotalFrames(frames);
	}

	public void addFrames(int frames) {
		model.addFrames(frames);
	}

	private void deselect() {
		selectionBox = null;
		if (selectedSegments == null)
			return;

		for (LineSegment l : selectedSegments)
			l.setColour(Color.BLACK);
		selectedSegments = null;
	}

	private boolean selected() {
		return (selectionBox != null);
	}

	public void mousePressed(MouseEvent e) {
		mouseDown = true;

		if (mode == DrawMode.DRAW) {
			deselect();

			drawBuffer = new LineSegment(Color.BLACK, drawStroke);
			drawBuffer.addPoint(e.getPoint());
			this.repaint(this.getBounds());
		}
		else if (mode == DrawMode.SELECT) {
			if (selected() && selectionBox.contains(e.getPoint())) {
				mouseStart = e.getPoint();
				mouseEnd = e.getPoint();
			}
			else {
				deselect();
				drawBuffer = new LineSegment(Color.BLUE, selectStroke);
				drawBuffer.addPoint(e.getPoint());
				this.repaint(this.getBounds());
			}
		}
		else if (mode == DrawMode.ERASE) {
			deselect();
			mouseStart = e.getPoint();
			mouseEnd = e.getPoint();
		}
	}

	public void mouseReleased(MouseEvent e) {
		mouseDown = false;

		if (mode == DrawMode.DRAW) {
			model.addShape(drawBuffer, currentFrame);
			drawBuffer = null;
		}
		else if (mode == DrawMode.SELECT) {
			if (!selected()) {
				selectionBox = drawBuffer.getPolygon();
				selectedSegments = model.getSelectedShapes(selectionBox, currentFrame);

				for (LineSegment l : selectedSegments)
					l.setColour(Color.BLUE);

				drawBuffer = null;
			}
			else {
				Delta d = new Delta(mouseStart, e.getPoint());
				if (selectedSegments != null) {
					for(LineSegment l : selectedSegments) {
						model.storeFrame(l, d, currentFrame);
						model.overwriteFrames(l, currentFrame);
					}
				}
			}
			this.repaint(this.getBounds());
		}
		else if (mode == DrawMode.ERASE) {
			model.eraseLine(mouseStart, mouseEnd, currentFrame);
			this.repaint(this.getBounds());
		}
	}

	public void mouseDragged(MouseEvent e) {

		if (mode == DrawMode.DRAW) {
			drawBuffer.addPoint(e.getPoint());
			this.repaint(this.getBounds());
		}
		else if (mode == DrawMode.SELECT) {
			if (!selected()) {
				drawBuffer.addPoint(e.getPoint());
				this.repaint(this.getBounds());
			}
			else {
				mouseEnd = e.getPoint();
				Delta d = new Delta(mouseStart, mouseEnd);
				if (selectedSegments != null) {
					for (LineSegment l : selectedSegments) {
						model.storeFrame(l, d, currentFrame);
					}
				}

				this.repaint(this.getBounds());
			}
		}
		else if (mode == DrawMode.ERASE) {
			mouseEnd = e.getPoint();
			this.repaint(this.getBounds());
		}
	}

	public void mouseMoved(MouseEvent e) {}

	public void mouseClicked(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}
}