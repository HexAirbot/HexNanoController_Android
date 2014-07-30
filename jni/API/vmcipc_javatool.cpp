#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include <android/log.h>
#include "vmcipc_javatool.h"


namespace javatool
{
//void SetJniToRTMPConnectThread(bool bSet);
void JavaShowStatus(const char *s,int state);

static JNIEnv *g_envForRTMPConnectThread = NULL;

static JNIEnv *g_envMain = NULL;
static jobject g_objIpcProxy = 0;

static JavaVM* g_JavaVM = NULL;
static jmethodID g_methodShowStatus = 0;
static jmethodID g_methodSendMsgByJni = 0;
static jclass g_clsIpcProxy = 0;

#if 0
void SetJniToRTMPConnectThread(bool bSet)
{
	JNIEnv *env; 
	if(bSet)
	{
	 	g_JavaVM->AttachCurrentThread((JNIEnv**)&env, NULL);
		g_envForRTMPConnectThread = env;
	}
	else
	{
	 	g_JavaVM->DetachCurrentThread();
	}
}
#endif
void InitEnv(JNIEnv *env)
{
	if(!g_envMain)
	{
		g_envMain = env;
	}
}
void InitVM(JavaVM *vm)
{
	if(!g_JavaVM)
	{
		g_JavaVM = vm;
	}
}
void InitProxyObj(jobject objIpcProxy)
{
	
	if(!g_objIpcProxy)
	{
		g_objIpcProxy = g_envMain->NewGlobalRef(objIpcProxy);
	}
}

void InitJavaMethods()
{
    //找到java中的类
    if(g_methodShowStatus)
    {
    	goto end;
    }
    g_clsIpcProxy = g_envMain->FindClass("com/vmc/ipc/proxy/IpcProxy");
    g_methodShowStatus = g_envMain->GetMethodID(g_clsIpcProxy, "ipcConnectStateChanged", "(ILjava/lang/String;)V");
    g_methodSendMsgByJni = g_envMain->GetMethodID(g_clsIpcProxy, "sendMsgByJni", "(ILjava/lang/String;)V");
    if(g_methodShowStatus == NULL)
    {
		LOGW("%s error",__FUNCTION__);
        goto end;
    }
end:
	return ;
}

void JavaShowStatus(const char *s,int state)
{
	LOGW("%s %s %d",__FUNCTION__,s,state);
	JNIEnv *env;
	g_JavaVM->AttachCurrentThread((JNIEnv**)&env, NULL);
	jstring js = env->NewStringUTF(s);
    env->CallVoidMethod( g_objIpcProxy, g_methodShowStatus,state,js);
	env->DeleteLocalRef(js);
	g_JavaVM->DetachCurrentThread();
    //g_env->CallVoidMethod( g_obj, g_midShowStatus ,g_env->NewStringUTF("1.2.3.4"),g_env->NewStringUTF("1.2.3.4"));
}
void JavaSendMsgByJni(int cmd,const char *msg)
{
	//LOGW("%s %s %d",__FUNCTION__,msg,cmd);
	LOGW("%s %d %d",__FUNCTION__,cmd,strlen(msg));
	JNIEnv *env;
	g_JavaVM->AttachCurrentThread((JNIEnv**)&env, NULL);
	
	jstring js = env->NewStringUTF(msg);
    env->CallVoidMethod( g_objIpcProxy, g_methodSendMsgByJni,cmd,js);
	env->DeleteLocalRef(js);
	g_JavaVM->DetachCurrentThread();
    //g_env->CallVoidMethod( g_obj, g_midShowStatus ,g_env->NewStringUTF("1.2.3.4"),g_env->NewStringUTF("1.2.3.4"));
}


#if 0
static jobject g_objVideoSurface = 0;
static jmethodID g_methodSendMsgByJni = 0;
static jclass g_clsVideoSurface = 0;

void InitVideoSurfaceJavaMethods()
{
    g_clsVideoSurface = g_envMain->FindClass("com/vmc/ipc/view/VideoStageSurface");
    g_methodSendMsgByJni = g_envMain->GetMethodID(g_clsIpcProxy, "sendMsgByJni", "(ILjava/lang/String;)V");
    if(g_methodSendMsgByJni == NULL)
    {
		LOGW("%s error",__FUNCTION__);
        return;
    }
}
void JavaSendMsgByJni(int cmd,const char *msg)
{
	//LOGW("%s %s %d",__FUNCTION__,s,state);
	JNIEnv *env;
	g_JavaVM->AttachCurrentThread((JNIEnv**)&env, NULL);
	
	jstring js = env->NewStringUTF(msg);
    env->CallVoidMethod( g_objVideoSurface, g_methodSendMsgByJni,cmd,js);
	env->DeleteLocalRef(js);
	g_JavaVM->DetachCurrentThread();
    //g_env->CallVoidMethod( g_obj, g_midShowStatus ,g_env->NewStringUTF("1.2.3.4"),g_env->NewStringUTF("1.2.3.4"));
}
#endif

}
