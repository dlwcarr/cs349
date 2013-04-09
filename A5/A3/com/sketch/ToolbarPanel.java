package sketch;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.AbstractButton;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Dimension;

import java.awt.FlowLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ToolbarPanel extends JPanel implements ActionListener {

	private ToolbarListener delegate;

	private JButton drawButton, eraseButton, selectButton;
	private DrawMode mode;

	public ToolbarPanel(ToolbarListener delegate) {
		super();

		this.delegate = delegate;

		this.mode = DrawMode.DRAW;
		delegate.setDrawMode(mode);

		this.initUI();
	}

	private void initUI() {
		this.setLayout(new FlowLayout(FlowLayout.LEFT));

		drawButton = new JButton("Draw");
		drawButton.setVerticalTextPosition(AbstractButton.BOTTOM);
		drawButton.setHorizontalTextPosition(AbstractButton.CENTER);
		drawButton.setActionCommand("draw");
		drawButton.addActionListener(this);

		eraseButton = new JButton("Erase");
		eraseButton.setVerticalTextPosition(AbstractButton.BOTTOM);
		eraseButton.setHorizontalTextPosition(AbstractButton.CENTER);
		eraseButton.setActionCommand("erase");
		eraseButton.addActionListener(this);

		selectButton = new JButton("Select");
		selectButton.setVerticalTextPosition(AbstractButton.BOTTOM);
		selectButton.setHorizontalTextPosition(AbstractButton.CENTER);
		selectButton.setActionCommand("select");
		selectButton.addActionListener(this);

		this.add(drawButton);
		this.add(eraseButton);
		this.add(selectButton);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("draw"))
			mode = DrawMode.DRAW;
		else if (e.getActionCommand().equals("erase"))
			mode = DrawMode.ERASE;
		else
			mode = DrawMode.SELECT;

		delegate.setDrawMode(mode);
	}
}