#include <jni.h>
#include <android/log.h>
#include <pthread.h>

#include <android/native_window.h> // requires ndk r5 or newer
#include <android/native_window_jni.h> // requires ndk r5 or newer

#include <string>
#include <map>

#include "vmc_decoder_handler.h"
#include "vmc_rec_handler.h"
#include "vmc_jpegenc.h"


#include "vmcipc_cmd.h"
#include "vmcipc_javatool.h"
#include "vmcipc_videostream.h"
#include "vmcipc_debug.h"

//#include "yuv2rgb.h"
//#include "libyuv.h"
#include "vmcipc.h"
#include "vmcipc_defines.h"


#ifdef __cplusplus
extern "C" {
#endif



JNIEXPORT void JNICALL Java_com_vmc_ipc_view_VideoStageSurfaceHardware_nativeSetSurfaceView
  (JNIEnv *env, jobject thiz, jobject surface){
	LOGW("%s",__FUNCTION__);
	ANativeWindow *window = NULL;
#if 0	
	if(g_window)
	{
		//ANativeWindow_release(g_window);
		LOGW("%s err alreay has window %p",__FUNCTION__, g_window);
		//CloseRTMPConnection();
	}
#endif	
	javatool::InitEnv(env);
	javatool::InitJavaMethods();


	if(surface)
	{
		window = ANativeWindow_fromSurface( env, surface );
		LOGW("%s Got window %p",__FUNCTION__, window);
	}
	else
	{
		LOGW("%s surface is null",__FUNCTION__);
	
	}

	if(window != NULL)
	{
		/*
		static bool bFirst = true;
		if(bFirst)
		{
			bFirst = false;
		}
		else
		{
			return ;
		}*/
		int width = ANativeWindow_getWidth(window);
		int height = ANativeWindow_getHeight(window);

		LOGW("Got window %d %d", width,height);

		//LOGW("window buffer count %d",window);

//		H264DecoderSetNativeWindow(window);

//		g_objVideoSurface = env->NewGlobalRef(thiz);
//		InitVideoSurfaceJavaMethods();
#if 1


#ifndef SOFT_DECODE_ONLY	
//		H264DecoderStart(window,H264ReadHardDecCallback,H264DecodeDoneCallback,NULL);
//		VmcSetDecodeStrategy(DECODE_AUTO);
#else
//		VmcSetDecodeStrategy(DECODE_SOFTWARE);
#endif

		//CloseRTMPConnection();
		videostream::SetNativeWindow(window);
		VmcInitSysDecoder(window);

		//StartSoftDecoder();
		//StartRTMPThread();
		
#else
		StartRTMPThread();
#endif
//		ANativeWindow_release(g_window);
	}	
	else
	{
		/*void *result = NULL;
		g_bThreadRunning = false;
		CloseRTMPClient();
		if(g_tidRTMP)
		{
			pthread_join(g_tidRTMP,&result);
			g_tidRTMP = 0;
		}*/
		
		/*if(VmcGetAjustedDecodeMode() != DECODE_SOFTWARE)
		{
			CloseRTMPConnection();
		}*/
		VmcCloseSysDecoder();
		videostream::SetNativeWindow(NULL);
		
	}
}





JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved)
{
	LOGI("Library has been loaded");

	// Saving the reference to the java virtual machine
	//g_JavaVM = vm;
	javatool::InitVM(vm);
	// Return the JNI version
	return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL
JNI_OnUnload(JavaVM *vm, void *reserved)
{
//	g_JavaVM = NULL;

	LOGI("Library has been unloaded");
}

static JNIEnv *Adapter_GetEnv()
{
//    int status;
    JNIEnv *envnow;
//    status = g_JavaVM->GetEnv((void **) &envnow, JNI_VERSION_1_4);
//    if(status < 0)
//    {
//        status = g_JavaVM->AttachCurrentThread(&envnow, NULL);
//        if(status < 0)
//        {
//            return NULL;
//        }
////        g_bAttatedT = TRUE;
//    }
    return envnow;
}

static void DetachCurrent()
{
//    if(g_bAttatedT)
//    {
//        g_JavaVM->DetachCurrentThread();
//    }
}

void ipcConnectStateChanged(JNIEnv *env, jobject jobj,int state,char* info){
	jclass cls = env->GetObjectClass(jobj);
		jmethodID callback = env->GetMethodID(cls,"ipcConnectStateChanged","(ILjava/lang/String;)V");
		jstring infostr = env->NewStringUTF(info);
		env->CallVoidMethod(jobj,callback,state,infostr);
		env->DeleteLocalRef(infostr);
		env->DeleteLocalRef(cls);
}

/*
 * Class:     com_vmc_ipc_proxy_IpcProxy
 * Method:    connect
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_vmc_ipc_proxy_IpcProxy_connect
  (JNIEnv *env, jobject jobj,jstring address){
	LOGI("-->Java_com_vmc_ipc_proxy_IpcProxy_connect2");
	
	javatool::InitEnv(env);
	javatool::InitProxyObj(jobj);
	
	const char *tmp = env->GetStringUTFChars(address, NULL);
	LOGI("setaddress1: %s", tmp);
	strcpy(videostream::GetServerAddr(),tmp);
	env->ReleaseStringUTFChars(address, tmp);

	javatool::InitJavaMethods();
#if 1
	cmd::CommandConnectionInit();
	debug::StartDebugTimer();
	
#else
	ipcConnectStateChanged(env,jobj,1,"connect success");
#endif
}

JNIEXPORT void JNICALL Java_com_vmc_ipc_proxy_IpcProxy_setDecodeStrategy
  (JNIEnv *env, jobject jobj,jint stragety){
	LOGI("-->%s",__FUNCTION__);
	VmcSetDecodeStrategy(stragety);
}




/*
 * Class:     com_vmc_ipc_proxy_IpcProxy
 * Method:    pause
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_vmc_ipc_proxy_IpcProxy_pause
  (JNIEnv *, jobject){
	LOGI("-->Java_com_vmc_ipc_proxy_IpcProxy_pause");
}

/*
 * Class:     com_vmc_ipc_proxy_IpcProxy
 * Method:    resume
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_vmc_ipc_proxy_IpcProxy_resume
  (JNIEnv *, jobject){
	LOGI("-->Java_com_vmc_ipc_proxy_IpcProxy_resume");
}

/*
 * Class:     com_vmc_ipc_proxy_IpcProxy
 * Method:    disconnect
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_vmc_ipc_proxy_IpcProxy_disconnect
  (JNIEnv *, jobject){
	LOGI("-->Java_com_vmc_ipc_proxy_IpcProxy_disconnect");
	cmd::CommandConnectionDestroy();
}

/*
 * Class:     com_vmc_ipc_proxy_IpcProxy
 * Method:    triggerTakeOff
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_vmc_ipc_proxy_IpcProxy_triggerTakeOff
  (JNIEnv *, jobject){
	LOGI("-->Java_com_vmc_ipc_proxy_IpcProxy_triggerTakeOff");
}

/*
 * Class:     com_vmc_ipc_proxy_IpcProxy
 * Method:    startPreview
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_vmc_ipc_proxy_IpcProxy_startPreview
  (JNIEnv *, jobject){
	LOGI("-->Java_com_vmc_ipc_proxy_IpcProxy_startPreview");
	videostream::CloseRTMPConnection();
	videostream::StartSoftDecoder();
	videostream::StartRTMPThread();
}

/*
 * Class:     com_vmc_ipc_proxy_IpcProxy
 * Method:    stopPreview
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_vmc_ipc_proxy_IpcProxy_stopPreview
  (JNIEnv *, jobject){
	LOGI("-->Java_com_vmc_ipc_proxy_IpcProxy_stopPreview");
	videostream::CloseRTMPConnection();
}

/*
 * Class:     com_vmc_ipc_proxy_IpcProxy
 * Method:    setPreviewResolution
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_com_vmc_ipc_proxy_IpcProxy_setPreviewResolution
  (JNIEnv *, jobject, jint width, jint height){
	LOGI("-->Java_com_vmc_ipc_proxy_IpcProxy_setPreviewResolution %d %d",(int)width,(int)height);
	cmd::SendSetResolution((int)width,(int)height);
	cmd::SendRestartServer();
}

/*
 * Class:     com_vmc_ipc_proxy_IpcProxy
 * Method:    setBitrateControlType
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_vmc_ipc_proxy_IpcProxy_setBitrateControlType
  (JNIEnv *, jobject, jint type){
	LOGI("-->Java_com_vmc_ipc_proxy_IpcProxy_setBitrateControlType");
	int i;
	//¸ù¾Ýgstapi_ipc.h
	if(type == 1)
	{
		i = 2;
	}
	else
	if(type == 2)
	{
		i = 1;
	}
	cmd::SendSetBitrateControlType(i);

}

/*
 * Class:     com_vmc_ipc_proxy_IpcProxy
 * Method:    onSizeChange
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_com_vmc_ipc_proxy_IpcProxy_onSizeChange
  (JNIEnv *, jobject, jint, jint){
	LOGI("-->Java_com_vmc_ipc_proxy_IpcProxy_onSizeChange");
}

/*
 * Class:     com_vmc_ipc_proxy_IpcProxy
 * Method:    takePhoto
 * Signature: (Ljava/lang/String;Ljava/lang/String;Z)V
 */
JNIEXPORT void JNICALL Java_com_vmc_ipc_proxy_IpcProxy_takePhoto
  (JNIEnv * env, jobject, jstring dstDir, jstring name, jboolean){
	LOGI("-->Java_com_vmc_ipc_proxy_IpcProxy_takePhoto");
	std::string sDir,sName;	
	
	const char *tmp = env->GetStringUTFChars(dstDir, NULL);
	sDir = tmp;
	env->ReleaseStringUTFChars(dstDir, tmp);

	
	tmp = env->GetStringUTFChars(name, NULL);
	sName = tmp;
	env->ReleaseStringUTFChars(name, tmp);

	LOGI("Java_com_vmc_ipc_proxy_IpcProxy_takePhoto dir:%s name:%s",sDir.c_str(),sName.c_str());
#if 1	
	get_photo((char *)sDir.c_str(),(char *)sName.c_str(),false);
#else
	rapidjson::Document doc;
	char buf[50];
	doc.SetObject();
	doc.AddMember("cmd",(uint64_t)CMD_RESTART_SERVER,doc.GetAllocator());
	g_client->SendAsyn(doc,true);
#endif
}

/*
 * Class:     com_vmc_ipc_proxy_IpcProxy
 * Method:    startRecord
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V
 */
JNIEXPORT void JNICALL Java_com_vmc_ipc_proxy_IpcProxy_startRecord
  (JNIEnv *env, jobject, jstring dstDir, jstring, jstring name, jboolean){
	LOGI("-->Java_com_vmc_ipc_proxy_IpcProxy_startRecord");

	std::string sDir,sName;	
	
	//g_sRecName.clear();
	const char *tmp = env->GetStringUTFChars(dstDir, NULL);
	//g_sRecName += tmp;
	sDir = tmp;
	env->ReleaseStringUTFChars(dstDir, tmp);

	//g_sRecName += "/";
	
	tmp = env->GetStringUTFChars(name, NULL);
	//g_sRecName += tmp;
	sName = tmp;
	env->ReleaseStringUTFChars(name, tmp);

	LOGI("Java_com_vmc_ipc_proxy_IpcProxy_startRecord dir:%s name:%s",sDir.c_str(),sName.c_str());
	videostream::StartRec((char *)sDir.c_str(),(char *)sName.c_str());

	
}

/*
 * Class:     com_vmc_ipc_proxy_IpcProxy
 * Method:    stopRecord
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_vmc_ipc_proxy_IpcProxy_stopRecord
  (JNIEnv *, jobject){
	LOGI("-->Java_com_vmc_ipc_proxy_IpcProxy_stopRecord");
	close_rec_hander();
}

/*
 * Class:     com_vmc_ipc_proxy_IpcProxy
 * Method:    takePhotoRemote
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_com_vmc_ipc_proxy_IpcProxy_takePhotoRemote
  (JNIEnv * env, jobject, jboolean){
	LOGI("-->Java_com_vmc_ipc_proxy_IpcProxy_takePhotoRemote");
}

/*
 * Class:     com_vmc_ipc_proxy_IpcProxy
 * Method:    stopRecordRemote
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_vmc_ipc_proxy_IpcProxy_stopRecordRemote
  (JNIEnv *, jobject){
	LOGI("-->Java_com_vmc_ipc_proxy_IpcProxy_stopRecordRemote");
	cmd::SendStopRecord();
}

/*
 * Class:     com_vmc_ipc_proxy_IpcProxy
 * Method:    startRecordRemote
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_com_vmc_ipc_proxy_IpcProxy_startRecordRemote
  (JNIEnv *env, jobject, jboolean){
	LOGI("-->Java_com_vmc_ipc_proxy_IpcProxy_startRecordRemote");

	struct tm *tm_ptr,timestruct;
	time_t the_time;
	char buf[256];

	(void) time(&the_time);
	tm_ptr=localtime(&the_time);
	strftime(buf,256,"%Y%m%d%H%M%s.mp4",tm_ptr);
	cmd::SendStartRecord("/mnt/userdata/ipc/videos/",buf);
	
	//auto func = [] { printf("aaa\n"); }; 
	//func();
	//auto a = 1.1;
}

/*
 * Class:     com_vmc_ipc_proxy_IpcProxy
 * Method:    takeNavDataSnapshot
 * Signature: ()Lcom/vmc/ipc/proxy/NavData;
 */
JNIEXPORT jobjectArray JNICALL Java_com_vmc_ipc_proxy_IpcProxy_takeNavDataSnapshot
  (JNIEnv *env, jobject jobj,jobject navdata){
//	LOGI("-->Java_com_vmc_ipc_proxy_IpcProxy_takeNavDataSnapshot");

	jobjectArray args = 0;
	jstring 	 str;
	std::map<std::string,std::string> &map = cmd::StartServerStatusEnum();
	int i = 0,len = map.size();
	std::map<std::string,std::string>::iterator iter;
	
	args = (env)->NewObjectArray(len,(env)->FindClass("java/lang/String"),0);
#if 0
	for (Value::ConstValueIterator itr = g_serverStatus.Begin(); itr != g_serverStatus.End(); ++itr)
	{
		str = (env)->NewStringUTF(itr->GetString() );
		(env)->SetObjectArrayElement(args, i, str);
		i++;
	}
#endif
	for(iter = map.begin();iter != map.end(); iter++)
	{
		std::string s = iter->first;
		s += ":";
		s += iter->second;
		str = (env)->NewStringUTF(s.c_str() );
		(env)->SetObjectArrayElement(args, i, str);
		i++;
	}

	cmd::EndServerStatusEnum();
#if 0
	for (rapidjson::Value::ConstMemberIterator itr = g_serverStatus.MemberBegin(); itr != g_serverStatus.MemberEnd(); ++itr)
	{
		std::string s = itr->name.GetString();
		s += ",";
		s += itr->value.GetString();
		(env)->SetObjectArrayElement(args, i, s.c_str());
		i++;
		printf("Type of member %s is %s\n", itr->name.GetString(), itr->value.GetString());
	}
#endif
#if 0	
	for( i=0; i < len; i++ )
	{
		str = (env)->NewStringUTF(sa[i] );
		(env)->SetObjectArrayElement(args, i, str);
	}	
#endif
	return args;
}

/*
 * Class:     com_vmc_ipc_proxy_IpcProxy
 * Method:    setControlParameter
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_vmc_ipc_proxy_IpcProxy_sendMessage2Server
  (JNIEnv *, jobject, jobjectArray, jobjectArray){
	LOGI("-->Java_com_vmc_ipc_proxy_IpcProxy_sendMessage2Server");
}

/*
 * Class:     com_vmc_ipc_proxy_IpcProxy
 * Method:    takeConfigSnapshot
 * Signature: ()Lcom/vmc/ipc/proxy/VIConfig;
 */
JNIEXPORT jobject JNICALL Java_com_vmc_ipc_proxy_IpcProxy_takeConfigSnapshot
  (JNIEnv *, jobject jobj,jobject config){
	LOGI("-->Java_com_vmc_ipc_proxy_IpcProxy_takeConfigSnapshot");
	return config;
}

/*
 * Class:     com_vmc_ipc_proxy_IpcProxy
 * Method:    getConfigItem
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_vmc_ipc_proxy_IpcProxy_getConfigItem
  (JNIEnv *, jobject, jstring){
	LOGI("-->Java_com_vmc_ipc_proxy_IpcProxy_getConfigItem");
}

/*
 * Class:     com_vmc_ipc_proxy_IpcProxy
 * Method:    setConfigItem
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_vmc_ipc_proxy_IpcProxy_setConfigItem
  (JNIEnv *, jobject, jstring, jstring){
	LOGI("-->Java_com_vmc_ipc_proxy_IpcProxy_setConfigItem");
}

/*
 * Class:     com_vmc_ipc_proxy_IpcProxy
 * Method:    resetConfig
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_vmc_ipc_proxy_IpcProxy_resetConfig
  (JNIEnv *, jobject){
	LOGI("-->Java_com_vmc_ipc_proxy_IpcProxy_resetConfig");
}

///*
// * Class:     com_vmc_ipc_view_gl_GLBGVideoSprite
// * Method:    onUpdateVideoTextureNative
// * Signature: (II)Z
// */
//JNIEXPORT jboolean JNICALL Java_com_vmc_ipc_view_gl_GLBGVideoSprite_onUpdateVideoTextureNative
//  (JNIEnv *, jobject, jint, jint){
//	LOGI("-->Java_com_vmc_ipc_view_gl_GLBGVideoSprite_onUpdateVideoTextureNative");
//	return false;
//}
///*
// * Class:     com_vmc_ipc_view_gl_GLBGVideoSprite
// * Method:    onSurfaceCreatedNative
// * Signature: ()V
// */
//JNIEXPORT void JNICALL Java_com_vmc_ipc_view_gl_GLBGVideoSprite_onSurfaceCreatedNative
//  (JNIEnv *, jobject){
//	LOGI("-->Java_com_vmc_ipc_view_gl_GLBGVideoSprite_onSurfaceCreatedNative");
//}
//
///*
// * Class:     com_vmc_ipc_view_gl_GLBGVideoSprite
// * Method:    onSurfaceChangedNative
// * Signature: (II)V
// */
//JNIEXPORT void JNICALL Java_com_vmc_ipc_view_gl_GLBGVideoSprite_onSurfaceChangedNative
//  (JNIEnv *, jobject, jint, jint){
//	LOGI("-->Java_com_vmc_ipc_view_gl_GLBGVideoSprite_onSurfaceChangedNative");
//}
//
///*
// * Class:     com_vmc_ipc_view_gl_GLBGVideoSprite
// * Method:    getVideoFrameNative
// * Signature: (Landroid/graphics/Bitmap;[F)Z
// */
//JNIEXPORT jboolean JNICALL Java_com_vmc_ipc_view_gl_GLBGVideoSprite_getVideoFrameNative
//  (JNIEnv *, jobject, jobject, jfloatArray){
//	LOGI("-->Java_com_vmc_ipc_view_gl_GLBGVideoSprite_getVideoFrameNative");
//	return false;
//}
#ifdef __cplusplus
}
#endif
