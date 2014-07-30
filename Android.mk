
LOCAL_PATH := $(call my-dir)
LOCAL_PATH_VMCIPC := $(LOCAL_PATH)
$(warning LOCAL_PATH $(LOCAL_PATH))
include $(CLEAR_VARS)



.PHONY: VMCIPC

VMCIPC:
	cd $(LOCAL_PATH_VMCIPC) && ant debug
	cp $(LOCAL_PATH_VMCIPC)/bin/MainActivity-debug.apk $(VMWORKS_OUTPUT_PATH)/wificamclient.apk


$(LOCAL_PATH)/bin/MainActivity-debug.apk: VMCIPC
	

LOCAL_MODULE := wificamclient
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := PREBUILT
LOCAL_MODULE_PATH := $(PRODUCT_OUT)
LOCAL_SRC_FILES := bin/MainActivity-debug.apk
include $(BUILD_PREBUILT)



