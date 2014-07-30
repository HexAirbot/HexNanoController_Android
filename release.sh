#!/bin/sh
rm -fr ./jni/DEC/Android.mk
rm -fr ./jni/DEC/*.cpp
rm -fr ./jni/DEC/include/aosp
cp ./obj/local/armeabi/libvmcffmpegdec.a ./jni/API/ -a
