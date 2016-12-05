#!/usr/bin/env bash
set -x # print each command to terminal
set -e # fail after first error
if [ ! -e opencvSDK ]; then
  wget downloads.sourceforge.net/project/opencvlibrary/opencv-android/2.4.11/OpenCV-2.4.11-android-sdk.zip
  unzip OpenCV-2.4.11-android-sdk.zip -d opencvSDK
fi
cp -R opencvSDK/OpenCV-android-sdk/sdk/java/src openCVLibrary2411/src/main/java
cp -R opencvSDK/OpenCV-android-sdk/sdk/native/libs app/src/main/jniLibs

