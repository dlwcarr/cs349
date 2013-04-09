package com.sketch;

import java.lang.String;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.AbstractButton;
import javax.swing.JSlider;

import javax.swing.Timer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.BorderLayout;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TimePanel extends JPanel implements ActionListener, ChangeListener {
	private static final int FPS = 30;
	private static final int INITIAL_LENGTH = 10;
	private static final String rewindActionCommand = "rewind";
	private static final String playPauseActionCommand = "playpause";
	private static final String fastForwardActionCommand = "fastforward";
	private static final String timerActionCommand = "timer";
	private static final String addFramesCommand = "addframes";

	private TimeListener delegate;

	private PlayMode mode;

	private Timer timer;

	private JButton rewindButton, playPauseButton, fastForwardButton, addFramesButton;
	private JSlider timeSlider;

	public TimePanel(TimeListener delegate) {
		super();

		this.delegate = delegate;

		this.mode = PlayMode.PAUSE;

		timer = new Timer((int) 1000/FPS, this);
		timer.stop();
		timer.setInitialDelay(0);
		timer.setRepeats(true);
		timer.setActionCommand(timerActionCommand);

		this.initUI();
	}

	private void initUI() {
		this.setLayout(new BorderLayout());

		rewindButton = new JButton("Rewind");
		rewindButton.setActionCommand(rewindActionCommand);
		rewindButton.addActionListener(this);

		playPauseButton = new JButton("Play");
		playPauseButton.setActionCommand(playPauseActionCommand);
		playPauseButton.addActionListener(this);

		fastForwardButton = new JButton("Fast Forward");
		fastForwardButton.setActionCommand(fastForwardActionCommand);
		fastForwardButton.addActionListener(this);

		addFramesButton = new JButton("Add Frames");
		addFramesButton.setActionCommand(addFramesCommand);
		addFramesButton.addActionListener(this);

		timeSlider = new JSlider(JSlider.HORIZONTAL, 0, INITIAL_LENGTH * FPS, 0);
		timeSlider.setMajorTickSpacing(FPS);
		timeSlider.setPaintTicks(true);
		timeSlider.addChangeListener(this);

		JPanel tempPanel = new JPanel();
		tempPanel.add(rewindButton);
		tempPanel.add(playPauseButton);
		tempPanel.add(fastForwardButton);

		this.add(tempPanel, BorderLayout.WEST);
		this.add(timeSlider, BorderLayout.CENTER);
		this.add(addFramesButton, BorderLayout.EAST);
	}

	private void play() {
		mode = PlayMode.PLAY;
		timer.start();
		playPauseButton.setText("Pause");
		delegate.startPlaying();
	}

	private void pause() {
		mode = PlayMode.PAUSE;
		timer.stop();
		playPauseButton.setText("Play");
		delegate.stopPlaying();
	}

	public PlayMode getPlayMode() {
		return this.mode;
	}

	public int getTotalFrames() {
		return timeSlider.getMaximum();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(rewindActionCommand)) {
			this.pause();
			timeSlider.setValue(0);
		}
		else if (e.getActionCommand().equals(playPauseActionCommand) && mode == PlayMode.PLAY) {
			this.pause();
		}
		else if (e.getActionCommand().equals(playPauseActionCommand) && mode == PlayMode.PAUSE) {
			this.play();
		}
		else if (e.getActionCommand().equals(fastForwardActionCommand)) {
			this.pause();
			timeSlider.setValue(timeSlider.getMaximum());
		}
		else if (e.getActionCommand().equals(timerActionCommand)) {
			timeSlider.setValue(timeSlider.getValue() + 1);
		}
		else if (e.getActionCommand().equals(addFramesCommand)) {
			int frameCount = timeSlider.getMaximum() + FPS;
			timeSlider.setMaximum(frameCount);
			delegate.addFrames(frameCount);
		}
	}

	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		delegate.changeFrame((int)source.getValue());

		if (source.getValue() == source.getMaximum()) {
			this.pause();
		}
	}
}