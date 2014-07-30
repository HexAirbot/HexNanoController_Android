LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_LDLIBS += -llog  -lGLESv2 -ljnigraphics
LOCAL_MODULE    := vmcipc
LOCAL_SRC_FILES := vmcipc.cpp \
				gl_bg_video_sprite_stub.cpp \
				gl_video_stage.cpp \
				java_callbacks.cpp \
				vmcipc_videostream.cpp \
				vmcipc_cmd.cpp \
				vmcipc_javatool.cpp \
				vmcipc_debug.cpp \
				ftp_stub.cpp \
				config_stub.cpp \
				config/minIni.c \
				config/IPCConfig.cpp
				

#LOCAL_SHARED_LIBRARIES := libvmctffmpegdec
LOCAL_STATIC_LIBRARIES := libvmcffmpegdec
				
LOCAL_CFLAGS += -D__USE_GNU -D__linux__ -DNO_ARDRONE_MAINLOOP -DUSE_ANDROID -DTARGET_CPU_ARM=1 -DTARGET_CPU_X86=0 -DUSE_WIFI -DFFMPEG_SUPPORT -fstack-protector
LOCAL_CFLAGS += -DANDROID_NDK -include vmcipc_defines.h
LOCAL_CPP_FEATURES := exceptions

LOCAL_CPPFLAGS := -I$(LOCAL_PATH)/include -I$(LOCAL_PATH)/../DEC/ -I$(LOCAL_PATH)/../DEC/include/ -std=c++11

LOCAL_LDFLAGS += 	\
	-lc -lstdc++ -lcutils -lutils -landroid  \
	-L$(LOCAL_PATH) -L$(LOCAL_PATH)/../../obj/local/armeabi/ \
	-lstagefright_foundation -lstagefright -lbinder -lui \
	-Xlinker "-(" \
	-lssl -lcrypto  -lboost_system -lboost_timer -lboost_chrono -lboost_thread \
	-lwa -lrtmptoolkit -lvmcffmpegdec  -lftp\
	-lavcodec -lavutil -lutils -lbinder -ljpeg -lavformat -lvnettool \
	$(NDK_ROOT)/sources/cxx-stl/gnu-libstdc++/4.6/libs/armeabi/libgnustl_static.a \
	 \
	-Xlinker "-)"

include $(BUILD_SHARED_LIBRARY)
