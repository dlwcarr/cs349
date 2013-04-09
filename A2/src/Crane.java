package com.cranesim;

import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.event.MouseEvent;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;

import java.awt.Color;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Rectangle;

public class Crane implements CraneComponent {
	private static int CRANE_HEIGHT = 150;
	private static int CRANE_WIDTH = 200;

	protected Color colour;

	protected double xOffset, yOffset;
	protected int x, y;
	protected Rectangle body;

	protected CraneArm arm;

	public Crane(int groundHeight) {
		this(groundHeight, Color.ORANGE);
	}

	public Crane(int groundHeight, Color c) {
		super();
		
		body = new Rectangle(0, 0, CRANE_WIDTH, CRANE_HEIGHT);
		x = 10;
		y = groundHeight - CRANE_HEIGHT;

		colour = c;
		arm = new CraneArm(x + CRANE_WIDTH - 30, y + 10, -1, colour);
		arm.addArms(3);
		arm.addMagnet();
	}

	public void paintCrane(Graphics graphics) {
		Graphics2D g = (Graphics2D)graphics.create();

		AffineTransform t = getTransform();
		g.setTransform(t);

		g.setColor(colour);
		g.fill(body);
		g.setColor(Color.BLACK);
		g.draw(body);

		arm.paintCrane(graphics);
	}

	public boolean contains(Point p) {
		Point tp = new Point();
		AffineTransform t = getTransform();
		try {
			t.inverseTransform(p, tp);
			if (body.contains(tp))
				return true;
			return false;
		}
		catch(NoninvertibleTransformException e) {
			System.out.println(e.toString());
			return false;
		}
	}

	public boolean checkMagnet() {
		if (arm == null)
			return false;
		return arm.checkMagnet();
	}

	public AffineTransform getMagnetTransform() {
		if (arm == null)
			return null;
		return arm.getMagnetTransform();
	}

	public Rectangle2D getMagnetRect() {
		if (arm == null)
			return null;
		return arm.getMagnetRect();
	}

	public void mouseClicked(Point p) {
		if (!this.contains(p)) {
			arm.mouseClicked(p);
			return;
		}

		xOffset = p.getX() - x;
		yOffset = p.getY() - y;	
	}

	public void mouseDragged(Point end) {
		if (!this.contains(end)) {
			arm.mouseDragged(end);
			return;
		}

		int oldX = x;
		x = (int)(end.getX() - xOffset);
		arm.translate(x - oldX);
	}

	public AffineTransform getTransform() {
		AffineTransform t = new AffineTransform();
		t.translate(x, y);

		return t;
	}

	public Color getColour() {
		return colour;
	}

	public void setColour(Color c) {
		colour = c;
	}

	public CraneArm getArm() {
		return arm;
	}
}