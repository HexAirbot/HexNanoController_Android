/*
 * IPCConfig.cpp
 *
 *  Created on: 2014年2月26日
 *      Author: Administrator
 */

#include "minIni.h"
#include <android/log.h>
#include <stdio.h>
#include <string.h>
#include "IPCConfig.h"
#include <sys/types.h>
#include <sys/stat.h>

#define TAG "IPCCONFIG"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_INFO, "vmc", __VA_ARGS__))
#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "vmc", __VA_ARGS__))
#define LOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN, "vmc", __VA_ARGS__))

static char fileName[256];
static minIni *ini = NULL;

int callback(const char *section, const char *key, const char *value, const void *userdata)
{
  LOGD("    1[%s]\t%s=%s\n", section, key, value);
  return 1;
}

bool initConfig(const char *dir){
	sprintf(fileName,"%s",dir);
	if(access(fileName,NULL) != 0){
		if (mkdir(fileName,0777)){
			LOGD("create config folder fail.");
		}
	}
	strcat(fileName,IPC_CONFIG_FILE);
	LOGD("fileName=%s",fileName);
	ini = new minIni(fileName);
	ini_browse(callback,NULL,fileName);
	return true;
}

bool setDecodeModeConfig(int mode){
	bool result = false;
	if(ini != NULL)
		result = ini->put(SECTION_CLIENT,KEY_DECODE_MODE,mode);
	else
		LOGD("inifile is null");
	return result;
}

int getDecodeModeConfig(){
	int result;
	if(ini != NULL)
		result = ini->geti(SECTION_CLIENT,KEY_DECODE_MODE,DEFAULT_DECODE_MODE);
	else
		LOGD("inifile is null");
	return result;
}

bool setRemoteRecordConfig(bool isTrue){
	bool result = false;
	if(ini != NULL){
		result = ini->put(SECTION_CLIENT,KEY_ISSTOREREMOTE,isTrue);
	}
	else
		LOGD("inifile is null");
	return result;

}

bool getRemoteRecordConfig(){
	bool result = false;
	if(ini != NULL){
		result = ini->getbool(SECTION_CLIENT,KEY_ISSTOREREMOTE,DEFAULT_ISSTOREREMOTE);
	}
	else
		LOGD("inifile is null");
	return result;
}

bool setResolutionConfig(const char *resolution){
	bool result = false;
		if(ini != NULL){
			result = ini->put(SECTION_CLIENT,KEY_RESOLUTION,resolution);
		}
		else
			LOGD("inifile is null");
		return result;
}

const char* getResolutionConfig(){
	std::string result;
	if(ini != NULL){
		result = ini->gets(SECTION_CLIENT,KEY_RESOLUTION,DEFAULT_RESOLUTION);
	}
	else
		LOGD("inifile is null");
	char resolution[50];
	LOGD("getresolutionconfig -- %s",strcpy(resolution,result.c_str()));
	return resolution;
}

bool setBitrateControlConfig(int mode){
	bool result = false;
	if(ini != NULL)
		result = ini->put(SECTION_CLIENT,KEY_BITRATE_CONTROL,mode);
	else
		LOGD("inifile is null");
	return result;
}

int getBitrateControlConfig(){
	int result;
	if(ini != NULL)
		result = ini->geti(SECTION_CLIENT,KEY_BITRATE_CONTROL,DEFAULT_BITRATE_CONTROL);
	else
		LOGD("inifile is null");
	return result;
}







