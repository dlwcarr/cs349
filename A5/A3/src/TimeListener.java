package com.sketch;

public interface TimeListener {
	public void startPlaying();
	public void stopPlaying();
	public void changeFrame(int frame);
	public void addFrames(int frames);
}