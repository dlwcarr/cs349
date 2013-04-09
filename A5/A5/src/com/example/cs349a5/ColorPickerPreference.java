package com.example.cs349a5;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ColorPickerPreference extends DialogPreference implements OnSeekBarChangeListener {
	
	public static final int DEFAULT_VALUE = 0xFFFFFFFF;
	
	private int initialColor;
		
	private View colorPreview;
	
	private SeekBar redBar;
	private SeekBar greenBar;
	private SeekBar blueBar;

	public ColorPickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setDialogLayoutResource(R.layout.preference_color);
		
		this.setPositiveButtonText(R.string.set);
		this.setNegativeButtonText(android.R.string.cancel);
		this.setDialogIcon(null);		
	}

	public ColorPickerPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setDialogLayoutResource(R.layout.preference_color);
		
		this.setPositiveButtonText(R.string.set);
		this.setNegativeButtonText(android.R.string.cancel);
		this.setDialogIcon(null);
	}
	
	private void setColor(int color) {
		colorPreview.setBackgroundColor(color);
		redBar.setProgress(Color.red(color));
		greenBar.setProgress(Color.green(color));
		blueBar.setProgress(Color.blue(color));
	}
	
	private int getColor() {
		return Color.rgb(redBar.getProgress(), greenBar.getProgress(), blueBar.getProgress());
	}
	
	@Override
	protected View onCreateDialogView() {
		View root = super.onCreateDialogView();
		colorPreview = root.findViewById(R.id.color_preview);
		redBar = (SeekBar)root.findViewById(R.id.red_seekbar);
		greenBar = (SeekBar)root.findViewById(R.id.green_seekbar);
		blueBar = (SeekBar)root.findViewById(R.id.blue_seekbar);
		
		redBar.setOnSeekBarChangeListener(this);
		greenBar.setOnSeekBarChangeListener(this);
		blueBar.setOnSeekBarChangeListener(this);
		
		setColor(this.getPersistedInt(DEFAULT_VALUE));
		
		return root;
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			persistInt(getColor());
		}
	}
	
	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		if (restorePersistedValue) {
			initialColor = this.getPersistedInt(DEFAULT_VALUE);
		}
		else {
			setColor((Integer) defaultValue);
			persistInt(getColor());
		}
	}
	
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInteger(index, DEFAULT_VALUE);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		// Check whether this Preference is persistent (continually saved)
		if (isPersistent()) {
			// No need to save instance state since it's persistent, use superclass state
			return superState;
		}
		
		// Create instance of custom BaseSavedState
		final SavedState myState = new SavedState(superState);
		// Set the state's value with the class member that holds current setting value
		myState.value = getColor();
		return myState;
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		// Check whether we saved the state in onSaveInstanceState
		if (state == null || !state.getClass().equals(SavedState.class)) {
			// Didn't save the state, so call superclass
			super.onRestoreInstanceState(state);
			return;
		}
		
		// Cast state to custom BaseSavedState and pass to superclass
		SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());
		
		// Set this Preference's widget to reflect the restored state
		setColor(myState.value);
	}
	
	private static class SavedState extends BaseSavedState {
	    // Member that holds the setting's value
	    // Change this data type to match the type saved by your Preference
	    int value;

	    public SavedState(Parcelable superState) {
	        super(superState);
	    }

	    public SavedState(Parcel source) {
	        super(source);
	        // Get the current preference's value
	        value = source.readInt();  // Change this to read the appropriate data type
	    }

	    @Override
	    public void writeToParcel(Parcel dest, int flags) {
	        super.writeToParcel(dest, flags);
	        // Write the preference's value
	        dest.writeInt(value);  // Change this to write the appropriate data type
	    }

	    // Standard creator object using an instance of this class
	    @SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR =
	            new Parcelable.Creator<SavedState>() {

	        public SavedState createFromParcel(Parcel in) {
	            return new SavedState(in);
	        }

	        public SavedState[] newArray(int size) {
	            return new SavedState[size];
	        }
	    };
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		colorPreview.setBackgroundColor(getColor());
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
}
