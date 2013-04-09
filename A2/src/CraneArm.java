package com.cranesim;

import java.lang.Math;

import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.Dimension;

public class CraneArm implements CraneComponent {
	private static int ARM_THICKNESS = 30;
	private static int ARM_LENGTH = 150;

	protected Color colour;

	protected double angle;
	protected int x, y;
	protected double xOffset, yOffset;

	protected Rectangle body;

	protected CraneArm next;
	protected CraneMagnet magnet;

	public CraneArm(int x, int y, double angle) {
		this(x, y, angle, Color.ORANGE);
	}

	public CraneArm(int x, int y, double angle, Color c) {
		super();

		this.x = x;
		this.y = y;
		this.angle = angle;
		body = new Rectangle(0, 0, ARM_LENGTH, ARM_THICKNESS);
		colour = c;
	}

	public void addMagnet() {
		if (next != null) {
			next.addMagnet();
			return;
		}

		AffineTransform t = getTransform();
		Point p = new Point(ARM_LENGTH, ARM_THICKNESS / 2);
		t.transform(p, p);

		magnet = new CraneMagnet((int)p.getX(), (int)p.getY(), angle + (Math.PI * 0.5), colour);
	}

	public boolean checkMagnet() {
		if (next == null && magnet == null)
			return false;
		if (magnet == null)
			return next.checkMagnet();
		return magnet.checkMagnet();
	}

	public AffineTransform getMagnetTransform() {
		if (next == null && magnet == null)
			return null;
		if (magnet == null)
			return next.getMagnetTransform();
		return magnet.getTransform();
	}

	public Rectangle2D getMagnetRect() {
		if (next == null && magnet == null) 
			return null;
		if (magnet == null)
			return next.getMagnetRect();
		return magnet.getMagnetRect();
	}

	public void addArms(int amount) {
		for (int i = 0; i < amount; i++)
			addArms();
	}

	public void addArms() {
		if (next == null) {
			Point p = new Point(ARM_LENGTH, ARM_THICKNESS / 2);
			AffineTransform t = getTransform();
			t.transform(p, p);
			next = new CraneArm((int)p.getX(), (int)p.getY(), angle + 0.3, colour);
		}
		else
			next.addArms();
	}

	public void setNext(CraneArm a) {
		next = a;
	}

	public CraneArm next() {
		return next;
	}

	public void paintCrane(Graphics graphics) {
		Graphics2D g = (Graphics2D)graphics.create();

		AffineTransform t = getTransform();
		g.setTransform(t);

		g.setColor(colour);
		g.fill(body);
		g.setColor(Color.BLACK);
		g.draw(body);

		g.setColor(colour);
		g.fillOval(-7, -7, ARM_THICKNESS + 14, ARM_THICKNESS + 14);
		g.setColor(Color.BLACK);
		g.drawOval(-7, -7, ARM_THICKNESS + 14, ARM_THICKNESS + 14);

		if (next != null) 
			next.paintCrane(graphics);
		if (magnet != null)
			magnet.paintCrane(graphics);
	}

	public boolean contains(Point p) {
		Point tp = new Point();
		AffineTransform t = getTransform();

		try {
			t.inverseTransform(p, tp);
			if (body.contains(tp)) {
				return true;
			}
			return false;
		}
		catch(NoninvertibleTransformException e) {
			System.out.println(e.toString());
			return false;
		}
	}

	public void translate(int x) {
		this.x += x;
		if (next != null)
			next.translate(x);
		if (magnet != null)
			magnet.translate(x);
	}

	public void rotate(double a) {
		this.angle += a;
		if (next != null)
			next.rotate(a);
		if (magnet != null)
			magnet.rotate(a);
	}

	public void mouseClicked(Point p) {
		if (!this.contains(p)) {
			if (next != null)
				next.mouseClicked(p);
			if (magnet != null)
				magnet.mouseClicked(p);
			return;
		}
		
		AffineTransform t = getTransform();
		Point tp = new Point();

		try {
			t.inverseTransform(p, tp);
			xOffset = tp.getX();
			yOffset = tp.getY();
		}
		catch(NoninvertibleTransformException e) {
			System.out.println(e.toString());
			return;
		}
	}

	public void updateOrigin(Point p) {
		x = (int) p.getX();
		y = (int) p.getY();

		Point p2 = new Point(ARM_LENGTH, ARM_THICKNESS / 2);
		AffineTransform t = getTransform();
		t.transform(p2, p2);
		if (next != null)
			next.updateOrigin(p2);
		if (magnet != null)
			magnet.updateOrigin(p2);
	}

	public void mouseDragged(Point end) {
		if (!this.contains(end)) {
			if (next != null)
				next.mouseDragged(end);
			if (magnet != null) {
				magnet.mouseDragged(end);
			}
			return;
		}

		AffineTransform t = getTransform();
		Point p = new Point(end);

		try {
			t.inverseTransform(end, p);
			double theta = Math.atan((p.getY() - yOffset) / p.getX());
			this.rotate(theta);

			Point p2 = new Point(ARM_LENGTH, ARM_THICKNESS / 2);
			t.transform(p2, p2);
			if (next != null)
				next.updateOrigin(p2);
			if (magnet != null)
				magnet.updateOrigin(p2);
		}
		catch(NoninvertibleTransformException e) {
			System.out.println(e.toString());
			return;
		}
	}

	public AffineTransform getTransform() {
		AffineTransform t = new AffineTransform();
		t.translate(x, y);
		t.rotate(angle);
		t.translate(ARM_THICKNESS / -2, ARM_THICKNESS / -2);

		return t;
	}

	public Color getColour() {
		return colour;
	}

	public void setColour(Color c) {
		colour = c;
	}
}