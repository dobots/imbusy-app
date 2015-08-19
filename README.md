# imbusy-app
App that detects when you're busy (by BLE) and will send this status to your contacts.


# Setup

## Android studio
This app was made in [Android studio](http://developer.android.com/sdk/index.html), so it's easiest to use that too.

## Getting this app
git clone https://github.com/dobots/imbusy-app.git dir/to/imbusy-app

## Getting bluenet library
git clone https://github.com/dobots/bluenet-lib-android.git dir/to/bluenet-lib-android
File -> New -> Module -> Android Library
	- library name: Bluenet
	- module name: bluenet
	- No activity

cd dir/to/imbusy-app
rm -r bluenet/
ln -s dir/to/bluenet-lib-android bluenet
