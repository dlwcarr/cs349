package com.cranesim;

import java.awt.Point;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public interface CraneComponent {
	public boolean contains(Point p);
	public void mouseClicked(Point p);
	public void mouseDragged(Point end);
	public void paintCrane(Graphics graphics);
	public AffineTransform getTransform();
}