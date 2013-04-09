package com.sketch;

import java.awt.Point;

public class Delta {
	private int x, y;

	public Delta() {
		this.x = 0;
		this.y = 0;
	}

	public Delta(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Delta(Point p1, Point p2) {
		this.x = (int)(p2.getX() - p1.getX());
		this.y = (int)(p2.getY() - p1.getY());
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public double getDX() {
		return (double)x;
	}

	public double getDY() {
		return (double)y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}
}