package com.sketch;

import com.google.gson.*;

import java.util.ArrayList;

import java.awt.Point;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Polygon;

import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;

public class LineSegment {
	private ArrayList<Point> points;
	
	private Color colour;
	private BasicStroke stroke;

	public LineSegment(Color c, BasicStroke s) {
		points = new ArrayList<Point>();
		setColour(c);
		setStroke(s);
	}

	public LineSegment(Color c) {
		this(c, new BasicStroke(1));
	}

	public LineSegment(BasicStroke s) {
		this(Color.BLACK, s);
	}

	public LineSegment() {
		this(Color.BLACK, new BasicStroke(1));
	}

	public JsonArray convertToJson() {
		JsonArray a = new JsonArray();
		JsonObject j;
		for(Point p : points) {
			j = new JsonObject();
			j.add("x", new JsonPrimitive(new Integer((int)p.getX())));
			j.add("y", new JsonPrimitive(new Integer((int)p.getY())));
			a.add(j);
		}

		return a;
	}

	public void addPoint(Point p) {
		points.add(new Point(p));
	}

	public void addPoint(int x, int y) {
		points.add(new Point(x, y));
	}

	public void setColour(Color c) {
		colour = c;
	}

	public Color getColour() {
		return colour;
	}

	public void setStroke(BasicStroke s) {
		stroke = s;
	}

	public BasicStroke getStroke() {
		return stroke;
	}

	public boolean inside(Polygon poly) {
		for(Point p : points) {
			if (!poly.contains(p)) 
				return false;
		}
		return true;
	}

	public boolean inside(Polygon poly, AffineTransform t) {
		Point p = new Point();
		for(Point p2 : points) {
			t.transform(p2, p);
			if (!poly.contains(p))
				return false;
		}
		return true;
	}

	public boolean intersectsLine(Point p1, Point p2) {
		if (points.size() < 2)
			return false;

		int x1 = (int)points.get(0).getX();
		int y1 = (int)points.get(0).getY();
		int x2, y2;
		for (int i = 1; i < points.size(); i++) {
			x2 = (int)points.get(i).getX();
			y2 = (int)points.get(i).getY();
			if (Line2D.linesIntersect(p1.getX(), p1.getY(), p2.getX(), p2.getY(), x1, y1, x2, y2))
				return true;
			x1 = x2;
			y1 = y2;
		}
		return false;
	}

	public Polygon getPolygon() {
		Polygon poly = new Polygon();
		for(Point p : points)
			poly.addPoint((int)p.getX(), (int)p.getY());
		return poly;
	}

	public void paint(Graphics graphics, AffineTransform t) {
		if (points.size() < 1)
			return;

		Graphics2D g = (Graphics2D)graphics.create();
		g.setColor(colour);
		g.setStroke(stroke);
		g.setTransform(t);
		
		if (points.size() == 1) {
			g.drawLine((int)points.get(0).getX(), (int)points.get(0).getY(), (int)points.get(0).getX(), (int)points.get(0).getY());
			return;
		}
		
		int x1 = (int)points.get(0).getX();
		int y1 = (int)points.get(0).getY();
		int x2, y2;
		for (int i = 1; i < points.size(); i++) {
			x2 = (int)points.get(i).getX();
			y2 = (int)points.get(i).getY();
			g.drawLine(x1, y1, x2, y2);
			x1 = x2;
			y1 = y2;
		}
	}

	public void paint(Graphics graphics) {
		this.paint(graphics, new AffineTransform());
	}
}