#!/bin/bash

./gradlew installDebug
for var in "$@"
do
#	adb -s $var shell am start -n io.fluentic.ubicdn/.ui.splash.SplashActivity
#	adb -s $var shell am startservice -n io.fluentic.ubicdn/.data.UbiCDNService --ez "source" 0 --ez "wd" 0 --ez "bt" 1
	adb -s $var shell am start -n network.datahop.localsharing/.ui.splash.SplashActivity
done
