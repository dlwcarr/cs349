package com.example.cs349a5;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Matrix;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class MainActivity extends Activity implements OnSeekBarChangeListener, OnSharedPreferenceChangeListener{
	private static final String TAG = "MainActivity";
	private static final int LOAD_ANIMATION_REQUEST = 0x00000001;
	
	private SeekBar seekbar;
	private ImageButton playpauseButton;
	private AnimationView canvas;
	
	private boolean playing;
	
	private Handler timerHandler;
	private Runnable timerTask;
	private int frameLength;
	
	private int frameCount;
	private ArrayList<Path> shapes;
	private HashMap<Path, ArrayList<Matrix>> transforms;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		this.setTitle("Animation Viewer");
		
		playing = false;
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		int framerate = settings.getInt(getResources().getString(R.string.framerate_prefs_key), FrameratePickerPreference.DEFAULT_VALUE);
		frameLength = (int) (1000 / framerate);
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		
		
		timerHandler = new Handler();
		timerTask = new Runnable() {
			public void run() {
				incrementFrame();
				if (playing)
					timerHandler.postDelayed(timerTask, frameLength);
			}
		};
		
		shapes = new ArrayList<Path>();
		transforms = new HashMap<Path, ArrayList<Matrix>>();
		
		seekbar = (SeekBar) findViewById(R.id.animation_seekbar);
		playpauseButton = (ImageButton) findViewById(R.id.playpause_button);
		canvas = (AnimationView) findViewById(R.id.animation_view);
		
		canvas.setBackgroundColor(settings.getInt(getResources().getString(R.string.color_prefs_key), ColorPickerPreference.DEFAULT_VALUE));
		
		seekbar.setOnSeekBarChangeListener(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		int framerate = settings.getInt(getResources().getString(R.string.framerate_prefs_key), FrameratePickerPreference.DEFAULT_VALUE);
		frameLength = (int) (1000 / framerate);
		
		canvas.setBackgroundColor(settings.getInt(getResources().getString(R.string.color_prefs_key), ColorPickerPreference.DEFAULT_VALUE));

		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
		pause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void setFrame(int frame) {
		ArrayList<Path> a = new ArrayList<Path>();
		for (Map.Entry<Path, ArrayList<Matrix>> entry : transforms.entrySet()) {
			if (entry.getValue().get(frame) == null) continue;
			
			Path p = new Path(entry.getKey());
			p.transform(entry.getValue().get(frame));
			a.add(p);
		}
		
		canvas.setShapes(a);
	}
	
	private void incrementFrame() {
		Log.d(TAG, seekbar.getProgress() + "/" + seekbar.getMax());
		if (seekbar.getProgress() == seekbar.getMax()) {
			this.pause();
		}
		else if (seekbar.getProgress() < seekbar.getMax()) {
			seekbar.setProgress(seekbar.getProgress() + 1);
		}
	}
	
	private void play() {
		if (playing || seekbar.getProgress() == seekbar.getMax()) return;
		
		playpauseButton.setImageResource(R.drawable.av_pause);
		playpauseButton.setContentDescription(getResources().getString(R.string.pause));
		
		playing = true;
		timerHandler.removeCallbacks(timerTask);
		timerHandler.postDelayed(timerTask, frameLength);
	}
	
	private void pause() {
		if (!playing) return;
		
		playpauseButton.setImageResource(R.drawable.av_play);
		playpauseButton.setContentDescription(getResources().getString(R.string.play));
		
		playing = false;
		timerHandler.removeCallbacks(timerTask);
	}
	
	public void onLoadAnimationClick(MenuItem item) {		
		Intent intent = new Intent(this, FileExplorerActivity.class);
		startActivityForResult(intent, LOAD_ANIMATION_REQUEST);
	}
	
	public void onSettingsItemClick(MenuItem item) {		
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
	
	public void onRewindClick(View view) {
		this.pause();
		seekbar.setProgress(0);
	}
	
	public void onPlayPauseClick(View view) {
		if (playing) {
			this.pause();
		}
		else {
			this.play();
		}
	}
	
	public void onFastforwardClick(View view) {
		this.pause();
		seekbar.setProgress(seekbar.getMax());
	}
	
	private void initDataStructures(JSONObject json) {
		if (!json.has("frames") || !json.has("shapes"))
			return;
		try {
			this.shapes.clear();
			this.transforms.clear();
			
			frameCount = json.getInt("frames") - 1;
			seekbar.setProgress(0);
			seekbar.setMax(frameCount);
			
			JSONArray shapes = json.getJSONArray("shapes");
			Path p;
			JSONObject shape;
			JSONArray points;
			JSONObject point;
			JSONArray transforms;
			JSONObject transform;
			ArrayList<Matrix> t;
			Matrix m;
			for (int i = 0; i < shapes.length(); i++) {
				shape = shapes.getJSONObject(i);
				p = new Path();
				
				points = shape.getJSONArray("points");
				for (int j = 0; j < points.length(); j++) {
					point = points.getJSONObject(j);
					if (j == 0)
						p.moveTo((float) point.getInt("x"), (float) point.getInt("y"));
					else
						p.lineTo((float) point.getInt("x"), (float) point.getInt("y"));
				}
				
				this.shapes.add(p);
				
				transforms = shape.getJSONArray("transforms");
				t = new ArrayList<Matrix>();
				for (int j = 0; j < transforms.length(); j++) {
					if (transforms.isNull(j)) {
						t.add(null);
					}
					else {
						transform = transforms.getJSONObject(j);
						m = new Matrix();
						m.setTranslate((float) transform.getInt("x"), (float) transform.getInt("y"));
						t.add(m);
					}
				}
				
				this.transforms.put(p, t);
			}
			
			setFrame(seekbar.getProgress());
		}
		catch(JSONException e) {
			Toast.makeText(this, "Failed to load animation", Toast.LENGTH_LONG).show();
			Log.d(TAG, "json decode failed", e);
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "activity finished");
		if (requestCode == LOAD_ANIMATION_REQUEST) {
			if (resultCode == RESULT_OK) {
				String key = getResources().getString(R.string.animation_key);
				if (data.hasExtra(key)) {
					String json = data.getStringExtra(key);
					try {
						JSONObject animation = new JSONObject(json);
						initDataStructures(animation);
						Toast.makeText(this, "Sucessfully loaded animation", Toast.LENGTH_LONG).show();
					}
					catch(JSONException e) {
						Toast.makeText(this, "Failed to load animation", Toast.LENGTH_LONG).show();
						Log.d(TAG, "json decode failed", e);
					}
				}
			}
			else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Animation load cancelled", Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		setFrame(progress);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		this.pause();
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (getResources().getString(R.string.framerate_prefs_key).equals(key)) {
			int framerate = sharedPreferences.getInt(getResources().getString(R.string.framerate_prefs_key), FrameratePickerPreference.DEFAULT_VALUE);
			this.frameLength = (int) (1000 / framerate);
		}
		else if (getResources().getString(R.string.color_prefs_key).equals(key)) {
			canvas.setBackgroundColor(sharedPreferences.getInt(getResources().getString(R.string.color_prefs_key), ColorPickerPreference.DEFAULT_VALUE));
		}
	}

}
