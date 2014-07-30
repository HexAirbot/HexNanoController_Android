/*
 * common.h
 *
 *  Created on: May 4, 2011
 *      Author: Dmytro Baryskyy
 */

#ifndef COMMON_H_
#define COMMON_H_

#define  TAG    "IPC_JNI"
#ifdef _DEBUG_
#define LOGV(TAG, ...) ((void)__android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__))
#define LOGD(TAG, ...) ((void)__android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__))
#define LOGI(TAG, ...) ((void)__android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__))
#define LOGW(TAG, ...) ((void)__android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__))
#define LOGE(TAG, ...) ((void)__android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__))
#else
#define LOGD(...) ;
#define LOGV(...) ;
#define LOGI(...) ;
#define LOGW(...) ;
#define LOGE(...) ;
#endif

#define VIDEO_WIDTH   320
#define VIDEO_HEIGHT  240

// How many times a second to refresh the screen
#define kFPS 30		// Frame per second
#define kAPS 40		// Number of accelerometer() function calls by second

//extern uint16_t default_image[VIDEO_WIDTH*VIDEO_HEIGHT];
// This enum should match the constants defined in com.parrot.freeflight.drone.NavData class
typedef enum _ERROR_STATE_
{
	ERROR_STATE_NONE,
	ERROR_STATE_NAVDATA_CONNECTION,
	ERROR_STATE_START_NOT_RECEIVED,
	ERROR_STATE_EMERGENCY_CUTOUT,
	ERROR_STATE_EMERGENCY_MOTORS,
	ERROR_STATE_EMERGENCY_CAMERA,
	ERROR_STATE_EMERGENCY_PIC_WATCHDOG,
	ERROR_STATE_EMERGENCY_PIC_VERSION,
	ERROR_STATE_EMERGENCY_ANGLE_OUT_OF_RANGE,
	ERROR_STATE_EMERGENCY_VBAT_LOW,
	ERROR_STATE_EMERGENCY_USER_EL,
	ERROR_STATE_EMERGENCY_ULTRASOUND,
	ERROR_STATE_EMERGENCY_UNKNOWN,
	ERROR_STATE_ALERT_CAMERA,
	ERROR_STATE_ALERT_VBAT_LOW,
	ERROR_STATE_ALERT_ULTRASOUND,
	ERROR_STATE_ALERT_VISION,
	ERROR_STATE_MAX
} ERROR_STATE;


// This enum should match the constants defined in com.parrot.freeflight.drone.NavData class
typedef enum {
	NO_ALERT = 0,
	VIDEO_CONNECTION_ALERT,
	BATTERY_LOW_ALERT,
	ULTRASOUND_ALERT,
	VISION_ALERT,
	START_NOT_RECEIVED,
	CONTROL_LINK_NOT_AVAILABLE,
	WIFI_NOT_AVAILABLE
} ARDRONE_ALERT_STATE;


// This enum should match the constants defined in com.parrot.freeflight.service.DroneControlService
typedef enum {
	CONTROL_SET_YAW = 0,
	CONTROL_SET_GAZ,
	CONTROL_SET_PITCH,
	CONTROL_SET_ROLL
} CONTROL_COMMAND;


typedef enum {
	ID_PAIRING_BTN = 0,
	ID_NETWORK_NAME_EDIT,
	ID_ALTITUDE_LIMITED_BTN,
	ID_ADAPTIVE_VIDEO_BTN,
	ID_OUTDOOR_HULL_BTN,
	ID_OUTDOOR_FLIGHT_BTN
} UI_CONTROL_ID;


// This enum should match DroneProxy.EVideoRecorderCapability enum.
typedef enum {
	VIDEO_RECORDING_NOT_SUPPORTED,
	VIDEO_RECORDING_360P,
	VIDEO_RECORDING_720p
} VIDEO_RECORDING_CAPABILITY;


// Standard library
#include <stdlib.h>
#include <stdio.h>
#include <sys/time.h>
#include <time.h>

// JNI
#include <jni.h>

// Android
#include <android/log.h>


// This variable holds reference to the java virtual machine
extern JavaVM* g_vm;

#endif /* COMMON_H_ */
