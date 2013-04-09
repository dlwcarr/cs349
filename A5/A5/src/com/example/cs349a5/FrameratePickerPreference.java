package com.example.cs349a5;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

public class FrameratePickerPreference extends DialogPreference {
	
	private static final int MAXFRAMES = 120;
	private static final int MINFRAMES = 1;
	public static final int DEFAULT_VALUE = 30;
	
	private int initialFramerate;
	private NumberPicker frameratePicker;
	
	public FrameratePickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setDialogLayoutResource(R.layout.preference_framerate);
		
		this.setPositiveButtonText(android.R.string.ok);
		this.setNegativeButtonText(android.R.string.cancel);
		this.setDialogIcon(null);
	}
	
	public FrameratePickerPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setDialogLayoutResource(R.layout.preference_framerate);
		
		this.setPositiveButtonText(android.R.string.ok);
		this.setNegativeButtonText(android.R.string.cancel);
		this.setDialogIcon(null);
	}
	
	@Override
	protected View onCreateDialogView() {
		View root = super.onCreateDialogView();
		frameratePicker = (NumberPicker) root.findViewById(R.id.framerate_picker);
		frameratePicker.setMaxValue(MAXFRAMES);
		frameratePicker.setMinValue(MINFRAMES);
		frameratePicker.setValue(this.getPersistedInt(DEFAULT_VALUE));
		return root;	
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			persistInt(frameratePicker.getValue());
			setSummary(frameratePicker.getValue() + " frames per second");
		}
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		if (restorePersistedValue) {
			initialFramerate = this.getPersistedInt(DEFAULT_VALUE);
		}
		else {
			frameratePicker.setValue((Integer) defaultValue);
			persistInt(frameratePicker.getValue());
		}
		
		setSummary(initialFramerate + " frames per second");
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
		myState.value = frameratePicker.getValue();
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
		frameratePicker.setValue(myState.value);
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
}
