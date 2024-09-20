#!/bin/bash

./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
