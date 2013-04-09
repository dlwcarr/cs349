package com.cranesim;

import java.util.ArrayList;

import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;

import java.awt.geom.Rectangle2D;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;

import javax.swing.event.MouseInputListener;

public class World extends JPanel implements MouseInputListener {

	private static final int SCREEN_WIDTH = 1024;
	private static final int SCREEN_HEIGHT = 768;

	private Rectangle ground;
	private Crane crane;

	private boolean mouseDown;
	private Point mouseStart;

	private ArrayList<CandyBlock> blocks;
	private CandyBlock attachedBlock;

	public World() {
		super();

		ground = new Rectangle(0, (int)(0.9*SCREEN_HEIGHT), SCREEN_WIDTH, (int)(0.1*SCREEN_HEIGHT));
		crane = new Crane((int)ground.getY());

		blocks = new ArrayList<CandyBlock>(6);
		blocks.add(new CandyBlock(400, (int)ground.getY() - 50, 100, 50, Color.PINK));
		blocks.add(new CandyBlock(500, (int)ground.getY() - 50, 100, 50, Color.PINK));
		blocks.add(new CandyBlock(600, (int)ground.getY() - 100, 50, 100, Color.PINK));
		blocks.add(new CandyBlock(660, (int)ground.getY() - 50, 100, 50, Color.PINK));
		blocks.add(new CandyBlock(800, (int)ground.getY() - 50, 100, 50, Color.PINK));
		blocks.add(new CandyBlock(900, (int)ground.getY() - 50, 100, 50, Color.PINK));
		blocks.add(new CandyBlock(1000, (int)ground.getY() - 50, 100, 50, Color.PINK));

		mouseDown = false;
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Java 2D Skeleton");
		frame.add(new World());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);

		Graphics2D g = (Graphics2D)graphics.create();
		g.clearRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

		g.setColor(Color.GREEN);
		g.fill(ground);

		for(int i = 0; i < blocks.size(); i++)
			blocks.get(i).drawBlock(graphics);

		crane.paintCrane(graphics);
	}

	public void mouseClicked(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {
		mouseDown = true;
		crane.mouseClicked(e.getPoint());
		if (crane.checkMagnet()) {
			Rectangle2D magnet = crane.getMagnetRect();
			for(int i = 0; i < blocks.size(); i++) {
				if (blocks.get(i).closeEnough(magnet)) {
					attachedBlock = blocks.get(i);
				}
			}
		}
		else {
			attachedBlock = null;
		}
		this.repaint();
	}

	public void mouseReleased(MouseEvent e) {
		mouseDown = false;
	}

	public void mouseDragged(MouseEvent e) {
		crane.mouseDragged(e.getPoint());
		if (attachedBlock != null) {
			attachedBlock.setTransform(crane.getMagnetTransform());
		}
		this.repaint();
	}

	public void mouseMoved(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}
}