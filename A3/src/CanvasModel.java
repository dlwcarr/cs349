package com.sketch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

public class CanvasModel {

	private ArrayList<LineSegment> shapes;
	private HashMap<LineSegment, ArrayList<AffineTransform>> transformations;
	private int totalFrames;

	public CanvasModel() {
		shapes = new ArrayList<LineSegment>();
		transformations = new HashMap<LineSegment, ArrayList<AffineTransform>>();
	}

	private void fillTransforms() {
		for(ArrayList<AffineTransform> transforms : transformations.values()) {
			transforms.ensureCapacity(totalFrames);

			boolean shapeExists = false;
			boolean fill = false;
			AffineTransform fillTransform = new AffineTransform();
			for (int i = 0; i < totalFrames; i++) {
				if (!shapeExists && transforms.get(i) != null)
					shapeExists = true;
				else if (shapeExists && transforms.get(i) == null) {
					fillTransform = transforms.get(i - 1);
					fill = true;
				}

				if (fill) {
					if (fillTransform == null)
						transforms.set(i, null);
					else
						transforms.set(i, new AffineTransform(fillTransform));
				}
			}
		}
	}

	public void setTotalFrames(int frames) {
		totalFrames = frames + 1;
		fillTransforms();
	}

	public void addFrames(int frames) {
		int oldFrames = totalFrames;
		totalFrames += frames;
		
		for(ArrayList<AffineTransform> transforms : transformations.values()) {
			transforms.ensureCapacity(totalFrames);

			AffineTransform t = transforms.get(oldFrames - 1);
			if (t == null) {
				for (int i = oldFrames; i < totalFrames; i++)
					transforms.add(i, null);
			}
			else {
				for (int i = oldFrames; i < totalFrames; i++)
					transforms.add(i, new AffineTransform(t));
			}
		}
	}

	public void addShape(LineSegment l, int frame) {
		shapes.add(l);
		ArrayList<AffineTransform> temp = new ArrayList<AffineTransform>(totalFrames);
		for (int i = 0; i < totalFrames; i++) {
			if (i < frame)
				temp.add(i, null);
			else 
				temp.add(i, new AffineTransform());
		}
			
		transformations.put(l, temp);
	}

	public void addShape(LineSegment l) {
		this.addShape(l, 0);
	}

	private double abs(double d) {
		if (d < 0)
			return d * -1;
		return d;
	}

	public void overwriteFrames(LineSegment l, int frame) {
		AffineTransform t = transformations.get(l).get(frame);
		for (int i = frame; i < totalFrames; i++) {
			if (transformations.get(l).get(frame) != null)
				transformations.get(l).get(frame).setToTranslation(t.getTranslateX(), t.getTranslateY());
		}
	}

	public void storeFrame(LineSegment l, Delta d, int frame) {
		if (transformations.get(l).get(frame) == null)
			transformations.get(l).set(frame, new AffineTransform());

		int lastFrame = 0;
		if (frame - 1 > 0)
			lastFrame = frame - 1;

		AffineTransform t = transformations.get(l).get(lastFrame);
		if (t == null)
			transformations.get(l).get(frame).setToTranslation(d.getDX(), d.getDY()); 
		else
			transformations.get(l).get(frame).setToTranslation(t.getTranslateX() + d.getDX(), t.getTranslateY() + d.getDY());
	}

	public ArrayList<LineSegment> getShapes() {
		return shapes;
	}

	public ArrayList<LineSegment> getSelectedShapes(Polygon selectionBox, int frame) {
		ArrayList<LineSegment> selected = new ArrayList<LineSegment>();
		for (Map.Entry<LineSegment, ArrayList<AffineTransform>> l : transformations.entrySet()) {
			if (l.getValue().get(frame) != null && l.getKey().inside(selectionBox, l.getValue().get(frame))) {
				selected.add(l.getKey());
			}
		}
		return selected;
	}

	public void eraseLine(Point p1, Point p2, int frame) {
		Point tp1 = new Point();
		Point tp2 = new Point();
		for (Map.Entry<LineSegment, ArrayList<AffineTransform>> entry : transformations.entrySet()) {
			if (entry.getValue().get(frame) == null) continue;

			try {
				entry.getValue().get(frame).inverseTransform(p1, tp1);
				entry.getValue().get(frame).inverseTransform(p2, tp2);

				if (entry.getKey().intersectsLine(tp1, tp2)) {
					for(int i = frame; i < totalFrames; i++) {
						entry.getValue().set(i, null);
					}
				}
			}
			catch (NoninvertibleTransformException e) {
				System.out.println(e.toString());
			}
		}
	}

	public void eraseLine(Point p1, Point p2) {
		Iterator<LineSegment> it = shapes.iterator();
		while(it.hasNext()) {
			if (it.next().intersectsLine(p1, p2)) {
				transformations.remove(it.next());
				it.remove();
			}
		}
	}

	public void drawLines(Graphics graphics, int frame) {
		for (Map.Entry<LineSegment, ArrayList<AffineTransform>> entry : transformations.entrySet()) {
			if (entry.getValue().get(frame) != null)
				entry.getKey().paint(graphics, entry.getValue().get(frame));
		}
	}
}	