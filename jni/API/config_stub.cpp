/*
 * config_stub.cpp
 *
 *  Created on: 2014年2月27日
 *      Author: Administrator
 */
#include <jni.h>
#include <android/log.h>
#include "com_vmc_ipc_config_VmcConfig.h"
#include "config/IPCConfig.h"
#include <string>
#include "vmcipc_defines.h"

#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_vmc_ipc_config_VmcConfig
 * Method:    isStoreRemote
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_vmc_ipc_config_VmcConfig_isStoreRemote
  (JNIEnv *env, jobject){
	unsigned char isRemote = 0;
	bool result = getRemoteRecordConfig();
	isRemote = !!result;
	return (jboolean)isRemote;
}

/*
 * Class:     com_vmc_ipc_config_VmcConfig
 * Method:    setStoreRemote
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_com_vmc_ipc_config_VmcConfig_setStoreRemote
  (JNIEnv *, jobject, jboolean isRemote){
	unsigned char value = isRemote;
	setRemoteRecordConfig((value!=0));
}

/*
 * Class:     com_vmc_ipc_config_VmcConfig
 * Method:    getResolution
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_vmc_ipc_config_VmcConfig_getResolution
  (JNIEnv *env, jobject){
	const char *result = getResolutionConfig();
	jstring str = env->NewStringUTF(result);
	return str;
}

/*
 * Class:     com_vmc_ipc_config_VmcConfig
 * Method:    setResolution
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_vmc_ipc_config_VmcConfig_setResolution
  (JNIEnv *env, jobject, jstring resolution){
	const char *value = env->GetStringUTFChars(resolution,NULL);
	setResolutionConfig(value);
	env->ReleaseStringUTFChars(resolution,value);
}

/*
 * Class:     com_vmc_ipc_config_VmcConfig
 * Method:    getDecodeMode
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_vmc_ipc_config_VmcConfig_getDecodeMode
  (JNIEnv *env, jobject){
	int result = getDecodeModeConfig();
	return result;
}

/*
 * Class:     com_vmc_ipc_config_VmcConfig
 * Method:    setDecodeMode
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_vmc_ipc_config_VmcConfig_setDecodeMode
  (JNIEnv *, jobject, jint mode){
	setDecodeModeConfig(mode);
}

/*
 * Class:     com_vmc_ipc_config_VmcConfig
 * Method:    getBitrateControl
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_vmc_ipc_config_VmcConfig_getBitrateControl
  (JNIEnv *, jobject){
	int result = getBitrateControlConfig();
	return result;
}

/*
 * Class:     com_vmc_ipc_config_VmcConfig
 * Method:    setBitrateControl
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_vmc_ipc_config_VmcConfig_setBitrateControl
  (JNIEnv *, jobject, jint mode){
	setBitrateControlConfig(mode);
}

/*
 * Class:     com_vmc_ipc_config_VmcConfig
 * Method:    initNativeConfig
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_vmc_ipc_config_VmcConfig_initNativeConfig
  (JNIEnv *env, jobject, jstring dir){
	const char* tmp = env->GetStringUTFChars(dir,NULL);
	initConfig(tmp);
	env->ReleaseStringUTFChars(dir,tmp);
}

#ifdef __cplusplus
}
#endif

