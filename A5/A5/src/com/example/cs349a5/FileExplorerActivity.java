package com.example.cs349a5;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileExplorerActivity extends ListActivity {
	
	private static final String TAG = "FileExplorerActivity";
	
	private File currentFile;
	private FileListAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_explorer);
		
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			currentFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			if (currentFile == null)
				fileError();
		}
		else {
			fileError();
		}
		
		adapter = new FileListAdapter(this);
		if (currentFile != null) {
			changeDirectory(currentFile);
		}
		
		setListAdapter(adapter);
	}
	
	private void changeDirectory(File dir) {
		adapter.clear();
		
		currentFile = dir;
		setTitle(currentFile.getName());
		
		File parent = currentFile.getParentFile();
		if (parent != null)
			adapter.add(parent);
		else
			adapter.add(currentFile);
		
		File[] fileList = currentFile.listFiles();
		if (fileList != null)
			adapter.addAll(fileList);
	}
	
	private void fileError() {
		Toast.makeText(getApplicationContext(), "Unable to read external storage", Toast.LENGTH_LONG).show();
		this.finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	@Override
	protected void onListItemClick(ListView list, View view, int position, long id) {
		File file = (File) getListAdapter().getItem(position);
		
		if (file.isFile()) {
			if (file.canRead()) {
				String[] parts = file.getName().split("\\.");
				if (parts.length > 1 && parts[parts.length - 1].equalsIgnoreCase("json")) {
					try {
						BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
						String line;
						StringBuffer buf = new StringBuffer();
						
						while((line = fileReader.readLine()) != null) {
							buf.append(line + "\n");
						}
						
						JSONObject json = new JSONObject(buf.toString());
						if (json.has("frames") && json.has("shapes")) {							
							Intent intent = new Intent();
							intent.putExtra(getResources().getString(R.string.animation_key), json.toString());
							this.setResult(RESULT_OK, intent);
							this.finish();
						}
						else {
							Toast.makeText(this, "Invalid animation file", Toast.LENGTH_LONG).show();
						}
					}
					catch(FileNotFoundException e) {
						String msg = file.toString() + " not found";
						Log.e(TAG, msg, e);
						Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
					}
					catch(IOException e) {
						String msg = "Error reading " + file.toString();
						Log.e(TAG, msg, e);
						Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
					}
					catch(JSONException e) {
						String msg = "Error reading " + file.getName();
						Log.e(TAG, msg, e);
						Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
					}
				}
				else {
					Toast.makeText(this, file.getName() + " is not an animation file", Toast.LENGTH_LONG).show();
				}
			}
			else {
				Toast.makeText(this, "Unable to open " + file.toString(), Toast.LENGTH_LONG).show();
			}
		}
		else if (file.isDirectory()) {
			changeDirectory(file);
		}
		else {
			Toast.makeText(this, "Unknown file type", Toast.LENGTH_LONG).show();
		}
	}
	
	private class FileListAdapter extends ArrayAdapter<File> {
		
		private Context context;
		
		public FileListAdapter(Context context) {
			super(context, R.layout.row_file);
			
			this.context = context;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View v;
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.row_file, parent, false);
			}
			else {
				v = convertView;
			}
			
			TextView textView = (TextView) v.findViewById(R.id.row_filename);
			File file = getItem(position);
			
			textView.setText(file.getName());
			int drawable;
			if (position == 0)
				drawable = R.drawable.navigation_collapse;
			else if (file.isDirectory())
				drawable = R.drawable.collections_collection;
			else
				drawable = R.drawable.collections_view_as_list;
			
			textView.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0);
			
			return v;
		}
	}
}
