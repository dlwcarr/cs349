package com.cranesim;

import java.lang.Math;

import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;

import java.awt.Color;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Rectangle;

public class CraneMagnet implements CraneComponent {
	private static int MAGNET_WIDTH = 100;
	private static int MAGNET_HEIGHT = 20;

	protected int x, y;
	protected double angle;

	protected Rectangle body;
	protected Color colour;
	protected Color colour2;

	protected boolean on;

	public CraneMagnet(int x, int y, double angle) {
		this(x, y, angle, Color.ORANGE);
	}

	public CraneMagnet(int x, int y, double angle, Color c) {
		super();

		this.x = x;
		this.y = y;
		this.angle = angle;
		body = new Rectangle(0, 0, MAGNET_WIDTH, MAGNET_HEIGHT);
		this.colour = c;
		this.colour2 = colour.brighter();
	}

	public void rotate(double a) {
		angle += a;
	}

	public void translate(int x) {
		this.x += x;
	}

	public void updateOrigin(Point p) {
		x = (int)p.getX();
		y = (int)p.getY();
	}

	public boolean contains(Point p) {
		Point tp = new Point(p);
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

	public void mouseClicked(Point p) {
		if (this.contains(p)) {
			if (on) {
				on = false;
				Color tmp = colour;
				colour = colour2;
				colour2 = tmp;
			}
			else {
				on = true;
				Color tmp = colour;
				colour = colour2;
				colour2 = tmp;
			}
		}
	}

	public void mouseDragged(Point p) {

	}

	public void paintCrane(Graphics graphics) {
		Graphics2D g = (Graphics2D)graphics.create();

		g.setTransform(getTransform());

		g.setColor(colour);
		g.fill(body);
		g.setColor(Color.BLACK);
		g.draw(body);
	}

	public Rectangle2D getMagnetRect() {
		return getTransform().createTransformedShape(body).getBounds2D();
	}

	public AffineTransform getTransform() {
		AffineTransform t = new AffineTransform();
		t.translate(x, y);
		t.rotate(angle);
		t.translate(MAGNET_WIDTH / -2, 0);

		return t;
	}

	public Color getColour() {
		return colour;
	}

	public void setColour(Color c) {
		colour = c;
	}

	public boolean checkMagnet() {
		return on;
	}
}