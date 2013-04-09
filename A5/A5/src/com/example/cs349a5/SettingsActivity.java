package com.example.cs349a5;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		setTitle("Settings");
		
		getFragmentManager().beginTransaction().replace(R.id.settings_content, new SettingsFragment()).commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

}
