Min SDK Version: 14 (Android 4.0)
Target SDK Version: 17 (Android 4.2)
SDK 14 was used to allow PreferenceFragments.  Development was not targeted at
tablets, so Honeycomb was skipped.

Testing was done on an LG Nexus 4 (1280x768) and Asus Nexus 7 (1280x800)

AVD testing done on API level 17 w/ Google APIs, 1280x800 resolution and
20mb SD card.

APK is located in A5/bin/ and can be used to install on a hardware device.

Data files can be transfered to a hardware device using Android File Transfer.
Location doesn't really matter, but the file explorer defaults to the Downloads
folder.

Data files can be loaded onto AVD SD card using DDMS file explorer. Path doesn't
really matter since you should be able to find it using the in-app file explorer
Included sdcard.img with 2 sample animations on it. The copy one contains an
invalid json structure to test validating.

Saved animations are placed in A3/Saved Animations as <current timestamp>.json

Enhancements
- icons in file explorer to differentiate folders and files
- config page uses PreferenceFragment class to automatically store values in
	SharedPreferences k/v store
- file validation based on extension and json parsing