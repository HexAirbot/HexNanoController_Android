/*
 * java_callbacks.c
 *
 *  Created on: Jan 31, 2012
 *      Author: "Dmytro Baryskyy"
 */
#include <jni.h>
#include "java_callbacks.h"
#include <common.h>

#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "native-api", __VA_ARGS__))
#define LOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN, "native-api", __VA_ARGS__))

#define NULL "0"

#ifdef __cplusplus
extern "C" {
#endif

//static const char* TAG = "JAVA_CALLBACKS";

//void parrot_java_callbacks_call_void_method(JNIEnv *env, jobject obj, const char* methodName)
//{
//	if (env == NULL || obj == NULL) {
//		LOGW(TAG, "env or obj is null");
//		return;
//	}
//
//	jclass cls = env->GetObjectClass(obj);
//	jmethodID mid = env->GetMethodID(cls, methodName, "()V");
//
//	if (mid == 0) {
//		LOGW(TAG, "Method not found");
//		return;
//	}
//
//	env->CallVoidMethod(obj, mid);
//
//	env->DeleteLocalRef(cls);
//}
//
//
//void parrot_java_callbacks_call_void_method_int_int(jobject obj, const char* methodName, int param1, int param2)
//{
//	JNIEnv* env = NULL;
//
////	if (g_vm != NULL)
////	{
////		(*g_vm)->GetEnv(g_vm, (void **)&env, JNI_VERSION_1_6);
////	}
//
//	if (env == NULL || obj == NULL) {
//		LOGW(TAG, "env or obj is null");
//		return;
//	}
//
//	jclass cls = env->GetObjectClass(obj);
//	jmethodID mid = env->GetMethodID(cls, methodName, "(II)V");
//
//	if (mid == 0) {
//		LOGW(TAG, "Method not found");
//		return;
//	}
//
//	env->CallVoidMethod( obj, mid, param1, param2);
//
//	env->DeleteLocalRef( cls);
//}
//
//
//void parrot_java_callbacks_call_void_method_string(JNIEnv *env, jobject obj, const char*methodName, const char* param)
//{
//	if (env == NULL || obj == NULL) {
//		LOGW(TAG, "env or obj is null");
//		return;
//	}
//
//	jclass cls = env->GetObjectClass(obj);
//	jmethodID mid = env->GetMethodID(cls, methodName, "(Ljava/lang/String;)V");
//
//	if (mid == 0) {
//		LOGW(TAG, "Method not found");
//		return;
//	}
//
//	jstring paramUrf8 = env->NewStringUTF(param);
//
//	env->CallVoidMethod(obj, mid, paramUrf8);
//	env->DeleteLocalRef(cls);
//}
//
//void parrot_java_callbacks_call_void_method_string_boolean(JNIEnv *env, jobject obj, const char*methodName, const char* param, bool param2)
//{
//	if (env == NULL || obj == NULL) {
//		LOGW(TAG, "env or obj is null");
//		return;
//	}
//
//	jclass cls = env->GetObjectClass(obj);
//	jmethodID mid = env->GetMethodID(cls, methodName, "(Ljava/lang/String;Z)V");
//
//	if (mid == 0) {
//		LOGW(TAG, "Method not found");
//		return;
//	}
//
//	jstring paramUrf8 = env->NewStringUTF(param);
//
//    jboolean boolJava = param2;
//
//	env->CallVoidMethod(obj, mid, paramUrf8, boolJava);
//	env->DeleteLocalRef(cls);
//}

void java_set_field_int(JNIEnv *env, jobject obj, const char* fieldName, jint value)
{
	jclass cls = env->GetObjectClass(obj);

	jfieldID fid  = env->GetFieldID(cls, fieldName,  "I");
	env->SetIntField(obj, fid, value);

	// Removing reference to the class instance
	env->DeleteLocalRef(cls);
}


void java_set_field_bool(JNIEnv *env, jobject obj, const char* fieldName, jboolean value)
{
	jclass cls = env->GetObjectClass(obj);

	jfieldID fieldId  = env->GetFieldID(cls, fieldName,  "Z");
	env->SetBooleanField(obj, fieldId, value);

	// Removing reference to the class instance
	env->DeleteLocalRef(cls);
}


jboolean java_get_bool_field_value(JNIEnv *env, jobject obj, const char* fieldName)
{
	jclass cls = env->GetObjectClass(obj);

	jfieldID fieldId = env->GetFieldID(cls, fieldName, "Z");
	jboolean value = env->GetBooleanField(obj, fieldId);

	env->DeleteLocalRef(cls);

	return value;
}

#ifdef __cplusplus
}
#endif
