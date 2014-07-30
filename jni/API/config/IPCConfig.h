/*
 * IPCConfig.h
 *
 *  Created on: 2014年2月27日
 *      Author: Administrator
 */

#ifndef IPCCONFIG_H_
#define IPCCONFIG_H_

#define IPC_CONFIG_FILE "ipc_config.ini"
#define SECTION_CLIENT "ipc_client"

#define KEY_DECODE_MODE     			"decode_mode"
#define KEY_ISSTOREREMOTE				"remote_record"
#define KEY_RESOLUTION					"resolution"
#define KEY_BITRATE_CONTROL				"bitrate_control"

#define DEFAULT_DECODE_MODE     		1
#define DEFAULT_ISSTOREREMOTE			false
#define DEFAULT_RESOLUTION				"1280,720"
#define DEFAULT_BITRATE_CONTROL			1

bool initConfig(const char *dir);

bool setDecodeModeConfig(int mode);

int getDecodeModeConfig();

bool setRemoteRecordConfig(bool isTrue);

bool getRemoteRecordConfig();

bool setResolutionConfig(const char *resolution);

const char* getResolutionConfig();

bool setBitrateControlConfig(int mode);

int getBitrateControlConfig();


#endif /* IPCCONFIG_H_ */
