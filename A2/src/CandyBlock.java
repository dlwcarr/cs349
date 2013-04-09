package com.cranesim;

import java.lang.Math;

import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;

import java.awt.Color;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Dimension;
import java.awt.Rectangle;


public class CandyBlock {

	protected double angle;
	protected int x, y;
	protected int height, width;

	protected Color colour;
	protected Rectangle body;

	protected AffineTransform transform;

	public CandyBlock(int x, int y, int height, int width, Color colour) {
		super();

		this.x = x;
		this.y = y;
		this.height = height;
		this.width = width;
		this.colour = colour;

		transform = new AffineTransform();
		transform.translate(x, y);
		transform.rotate(angle);
		transform.translate(width / 2, 0);

		body = new Rectangle(0, 0, height, width);
	}

	public void translate(int x) {
		this.x += x;

		transform = new AffineTransform();
		transform.translate(x, y);
		transform.rotate(angle);
		transform.translate(width / 2, 0);
	}

	public void rotate(double a) {
		this.angle += a;

		transform = new AffineTransform();
		transform.translate(x, y);
		transform.rotate(angle);
		transform.translate(width / 2, 0);
	}

	public void updateOrigin(Point p) {
		this.x = (int)p.getX();
		this.y = (int)p.getY();

		transform = new AffineTransform();
		transform.translate(x, y);
		transform.rotate(angle);
		transform.translate(width / 2, 0);
	}

	public void drawBlock(Graphics graphics) {
		Graphics2D g = (Graphics2D)graphics.create();

		g.setTransform(getTransform());

		g.setColor(colour);
		g.fill(body);
		g.setColor(Color.BLACK);
		g.draw(body);
	}

	public boolean closeEnough(Rectangle2D s) {
		Rectangle2D r = getTransform().createTransformedShape(body).getBounds2D();
		return s.intersects(r);
	}

	public AffineTransform getTransform() {
		return transform;
	}

	public void setTransform(AffineTransform t) {
		transform = new AffineTransform(t);
	}

	public Color getColour() {
		return colour;
	}

	public void setColour(Color c) {
		colour = c;
	}
}