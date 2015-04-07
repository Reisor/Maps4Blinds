#!/bin/sh
# this script just joins together the commands
# you need to build the SDK from the cli, install it on device,
# and run it.
gradle assembleDebug installDebug
adb shell am start -n "com.javel.maps4blinds/com.javel.maps4blinds.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
