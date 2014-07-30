/*
 * ftp_stub.cpp
 *
 *  Created on: 2014年1月22日
 *      Author: Administrator
 */

#include <com_vmc_ipc_ftp_FTPClient.h>
#include <android/log.h>
#include <string>
#include <map>
#include <ftplib.h>

#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "FTPClient", __VA_ARGS__))

#ifdef __cplusplus
extern "C" {
#endif

static JavaVM *javavm = NULL;
static netbuf *conn = NULL;
static jobject progressUpdateListener;

struct _fields {
	jmethodID onProgress;
	jmethodID onError;
};
static struct _fields fields;

struct TRANSFERFILE {
	uint32_t totleSize;
	jobject listener;
	JNIEnv *env;
    char *filename;
};

void throwFtpException(JNIEnv *env,const char *msg){
	jclass cls;
	cls = env->FindClass("com.vmc.ipc.ftp.FtpException");
	if(cls == NULL){
		LOGI("FtpException is not exist.");
		return;
	}
	env->ThrowNew(cls,msg);
}

static int transferCallback(netbuf *ctl, fsz_t xfered, void *arg){
	struct TRANSFERFILE *f = (struct TRANSFERFILE *) arg;
	LOGI("transferFile = %p",arg);
	if(!f->env)
		LOGI("there is no valid JNIEnv.");
	LOGI("env = %p,listener = %p,%p, totalSize = %d",f->env,&(f->listener),&progressUpdateListener,f->totleSize);
	JNIEnv *env = NULL;
	javavm->AttachCurrentThread(&env,NULL);
//	env = f->env;
	LOGI("there is no valid JNIEnv1.");
//    jclass cls = env->FindClass("com/vmc/ipc/ftp/FtpCallbackListener");
//    jclass cls = env->GetObjectClass(progressUpdateListener);
	LOGI("there is no valid JNIEnv2.");
//    fields.onError = f->env->GetMethodID(cls, "onError", "(Ljava/lang/String;)V");
//    jmethodID onProgress= env->GetMethodID(cls, "onProgress", "(II)V");
	LOGI("there is no valid JNIEnv3.");
//	env->CallVoidMethod(f->listener,onProgress,xfered,f->totleSize);
	env->CallVoidMethod(progressUpdateListener,fields.onProgress,xfered,f->totleSize);
	LOGI("there is no valid JNIEnv4.");
//	env->CallVoidMethod(listener,onProgress,progress,totle);
//	javavm->DetachCurrentThread();
	LOGI("there is no valid JNIEnv5.");
}

void initJavaMethod(JNIEnv *env){
	env->GetJavaVM(&javavm);
    jclass cls = env->FindClass("com/vmc/ipc/ftp/FtpCallbackListener");
    fields.onError = env->GetMethodID(cls, "onError", "(Ljava/lang/String;)V");
    fields.onProgress = env->GetMethodID(cls, "onProgress", "(II)V");
}

/*
 * Class:     com_vmc_ipc_ftp_FTPClient
 * Method:    FtpInit
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_vmc_ipc_ftp_FTPClient_FtpInit
  (JNIEnv *env, jobject){
	LOGI("---Java_com_vmc_ipc_ftp_FTPClient_FtpInit");
	FtpInit();
	initJavaMethod(env);
	return 0;
}

/*
 * Class:     com_vmc_ipc_ftp_FTPClient
 * Method:    FtpLastResponse
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_vmc_ipc_ftp_FTPClient_FtpLastResponse
  (JNIEnv *env, jobject){
	LOGI("---Java_com_vmc_ipc_ftp_FTPClient_FtpLastResponse");
	if(!conn)
		throwFtpException(env,"you do not connect to the host.");
	char *lastResponse = FtpLastResponse(conn);
	return env->NewStringUTF(lastResponse);
}

/*
 * Class:     com_vmc_ipc_ftp_FTPClient
 * Method:    FtpSysType
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_vmc_ipc_ftp_FTPClient_FtpSysType
  (JNIEnv *env, jobject){
	LOGI("---Java_com_vmc_ipc_ftp_FTPClient_FtpSysType");
	if(!conn){
		throwFtpException(env,"you do not connect to the host.");
	}
	char *buf;
	buf = (char*)malloc(1024);
	if(!FtpSysType(buf,1024,conn)){
		throwFtpException(env,"get FtpServer System type fail.");
	}
	jstring result = env->NewStringUTF(buf);
	env->DeleteLocalRef(result);
	free(buf);
	return result;
}

/*
 * Class:     com_vmc_ipc_ftp_FTPClient
 * Method:    FtpSize
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_com_vmc_ipc_ftp_FTPClient_FtpSize
  (JNIEnv *env, jobject, jstring path, jint mode){
	LOGI("---Java_com_vmc_ipc_ftp_FTPClient_FtpSize");
	if(!conn){
		throwFtpException(env,"you do not connect to the host.");
	}
	unsigned int size = -1;
	const char *pathStr = env->GetStringUTFChars(path,NULL);
	FtpSize(pathStr,&size,mode,conn);
	env->ReleaseStringUTFChars(path,pathStr);
	return size;
}

/*
 * Class:     com_vmc_ipc_ftp_FTPClient
 * Method:    FtpModDate
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_vmc_ipc_ftp_FTPClient_FtpModDate
  (JNIEnv *env, jobject, jstring path){
	LOGI("---Java_com_vmc_ipc_ftp_FTPClient_FtpModDate");
	if(!conn){
		throwFtpException(env,"you do not connect to the host.");
	}
	char buf[1024];
	const char *pathStr = env->GetStringUTFChars(path,NULL);
	FtpModDate(pathStr,buf,1024,conn);
	env->ReleaseStringUTFChars(path,pathStr);
	return env->NewStringUTF(buf);
}

/*
 * Class:     com_vmc_ipc_ftp_FTPClient
 * Method:    FtpSetCallback
 * Signature: (Lcom/vmc/ipc/ftp/FTPClient/FtpCallbackListener;)Ljava/lang/String;
 */
JNIEXPORT void JNICALL Java_com_vmc_ipc_ftp_FTPClient_FtpSetCallback
  (JNIEnv *env, jobject, jobject listener){
	LOGI("---Java_com_vmc_ipc_ftp_FTPClient_FtpSetCallback");
	if(!conn){
		throwFtpException(env,"you do not connect to the host.");
	}
	progressUpdateListener = env->NewGlobalRef(listener);
	struct TRANSFERFILE f;
	f.env = env;
	f.listener = env->NewGlobalRef(listener);
	f.totleSize = 1024 * 1024;
	FtpCallbackOptions opt;
	opt.cbFunc = transferCallback;
	opt.cbArg = &f;
	opt.idleTime = 1000;
	opt.bytesXferred = 1024*8;
	FtpSetCallback(&opt,conn);
}

/*
 * Class:     com_vmc_ipc_ftp_FTPClient
 * Method:    FtpClearCallback
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT void JNICALL Java_com_vmc_ipc_ftp_FTPClient_FtpClearCallback
  (JNIEnv *env, jobject listener){
	LOGI("---Java_com_vmc_ipc_ftp_FTPClient_FtpClearCallback");
	if(!conn){
		throwFtpException(env,"you do not connect to the host.");
	}
	FtpClearCallback(conn);
}

/*
 * Class:     com_vmc_ipc_ftp_FTPClient
 * Method:    FtpConnect
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_vmc_ipc_ftp_FTPClient_FtpConnect
  (JNIEnv *env, jobject, jstring host){
	LOGI("---Java_com_vmc_ipc_ftp_FTPClient_FtpConnect");
	const char *hostStr = env->GetStringUTFChars(host,NULL);
	int result = FtpConnect(hostStr,&conn);
	env->ReleaseStringUTFChars(host,hostStr);
	return result;
}

/*
 * Class:     com_vmc_ipc_ftp_FTPClient
 * Method:    FtpLogin
 * Signature: (Ljava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_vmc_ipc_ftp_FTPClient_FtpLogin
  (JNIEnv *env, jobject, jstring user, jstring pass){
	LOGI("---Java_com_vmc_ipc_ftp_FTPClient_FtpLogin");
	if(!conn){
		throwFtpException(env,"you do not connect to the host.");
	}
	const char *userStr = env->GetStringUTFChars(user,NULL);
	const char *passStr = env->GetStringUTFChars(pass,NULL);
	int result = FtpLogin(userStr,passStr,conn);
	env->ReleaseStringUTFChars(user,userStr);
	env->ReleaseStringUTFChars(pass,passStr);
	return result;
}

/*
 * Class:     com_vmc_ipc_ftp_FTPClient
 * Method:    FtpQuit
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_vmc_ipc_ftp_FTPClient_FtpQuit
  (JNIEnv *env, jobject){
	LOGI("---Java_com_vmc_ipc_ftp_FTPClient_FtpQuit");
	if(!conn){
		throwFtpException(env,"you do not connect to the host.");
	}
	FtpQuit(conn);
}

/*
 * Class:     com_vmc_ipc_ftp_FTPClient
 * Method:    FtpSetConnectionMode
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_vmc_ipc_ftp_FTPClient_FtpSetConnectionMode
  (JNIEnv *env, jobject, jint mode){
	LOGI("---Java_com_vmc_ipc_ftp_FTPClient_FtpSetConnectionMode");
	if(!conn){
		throwFtpException(env,"you do not connect to the host.");
	}
	return FtpOptions(mode,0,conn);
}

/*
 * Class:     com_vmc_ipc_ftp_FTPClient
 * Method:    FtpChdir
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_vmc_ipc_ftp_FTPClient_FtpChdir
  (JNIEnv *env, jobject, jstring path){
	LOGI("---Java_com_vmc_ipc_ftp_FTPClient_FtpChdir");
	if(!conn){
		throwFtpException(env,"you do not connect to the host.");
	}
	const char *pathStr = env->GetStringUTFChars(path,NULL);
	int result = FtpChdir(pathStr,conn);
	env->ReleaseStringUTFChars(path,pathStr);
	return result;
}

/*
 * Class:     com_vmc_ipc_ftp_FTPClient
 * Method:    FtpMkDir
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_vmc_ipc_ftp_FTPClient_FtpMkDir
  (JNIEnv *env, jobject, jstring path){
	LOGI("---Java_com_vmc_ipc_ftp_FTPClient_FtpMkDir");
	if(!conn){
		throwFtpException(env,"you do not connect to the host.");
	}
	const char *pathStr = env->GetStringUTFChars(path,NULL);
	int result = FtpMkdir(pathStr,conn);
	env->ReleaseStringUTFChars(path,pathStr);
	return result;
}

/*
 * Class:     com_vmc_ipc_ftp_FTPClient
 * Method:    FtpRmdir
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_vmc_ipc_ftp_FTPClient_FtpRmdir
  (JNIEnv *env, jobject, jstring path){
	LOGI("---Java_com_vmc_ipc_ftp_FTPClient_FtpRmdir");
	if(!conn){
		throwFtpException(env,"you do not connect to the host.");
	}
	const char *pathStr = env->GetStringUTFChars(path,NULL);
	int result = FtpRmdir(pathStr,conn);
	env->ReleaseStringUTFChars(path,pathStr);
	return result;
}

/*
 * Class:     com_vmc_ipc_ftp_FTPClient
 * Method:    FtpDir
 * Signature: (Ljava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_vmc_ipc_ftp_FTPClient_FtpDir
  (JNIEnv *env, jobject, jstring out, jstring path){
	LOGI("---Java_com_vmc_ipc_ftp_FTPClient_FtpDir");
	if(!conn){
		throwFtpException(env,"you do not connect to the host.");
	}
	const char *outFile = env->GetStringUTFChars(out,NULL);
	const char *pathStr = env->GetStringUTFChars(path,NULL);
	int result = FtpDir(outFile,pathStr,conn);
	env->ReleaseStringUTFChars(out,outFile);
	env->ReleaseStringUTFChars(path,pathStr);
	return result;
}

/*
 * Class:     com_vmc_ipc_ftp_FTPClient
 * Method:    FtpNlst
 * Signature: (Ljava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_vmc_ipc_ftp_FTPClient_FtpNlst
(JNIEnv *env, jobject, jstring out, jstring path){
	LOGI("---Java_com_vmc_ipc_ftp_FTPClient_FtpNlst");
	if(!conn){
		throwFtpException(env,"you do not connect to the host.");
	}
	const char *outFile = env->GetStringUTFChars(out,NULL);
	const char *pathStr = env->GetStringUTFChars(path,NULL);
	int result = FtpDir(outFile,pathStr,conn);
	env->ReleaseStringUTFChars(out,outFile);
	env->ReleaseStringUTFChars(path,pathStr);
	return result;
}

/*
 * Class:     com_vmc_ipc_ftp_FTPClient
 * Method:    FtpCDUp
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_vmc_ipc_ftp_FTPClient_FtpCDUp
  (JNIEnv *env, jobject){
	LOGI("---Java_com_vmc_ipc_ftp_FTPClient_FtpCDUp");
	if(!conn){
		throwFtpException(env,"you do not connect to the host.");
	}
	return FtpCDUp(conn);
}

/*
 * Class:     com_vmc_ipc_ftp_FTPClient
 * Method:    FtpPwd
 * Signature: ()Ljava/lang/String
 */
JNIEXPORT jstring JNICALL Java_com_vmc_ipc_ftp_FTPClient_FtpPwd
  (JNIEnv *env, jobject){
	LOGI("---Java_com_vmc_ipc_ftp_FTPClient_FtpPwd");
	if(!conn){
		throwFtpException(env,"you do not connect to the host.");
	}
	char pathStr[1024];
	int result = FtpPwd(pathStr,1024,conn);
	return env->NewStringUTF(pathStr);
}

/*
 * Class:     com_vmc_ipc_ftp_FTPClient
 * Method:    FtpGet
 * Signature: (Ljava/lang/String;Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_com_vmc_ipc_ftp_FTPClient_FtpGet
  (JNIEnv *env, jobject, jstring out, jstring path, jint mode){
	LOGI("---Java_com_vmc_ipc_ftp_FTPClient_FtpGet");
	if(!conn){
		throwFtpException(env,"you do not connect to the host.");
	}
	const char *pathStr = env->GetStringUTFChars(path,NULL);
	const char *output = env->GetStringUTFChars(out,NULL);
	LOGI("out=%s /n path=%s /n mode=%d",output,pathStr,mode);
	int result = FtpGet(output,pathStr,mode,conn);
//	int result = FtpGet("/sdcard/qw/videos/11.mp4","/ipc/videos/1.mp4",FTPLIB_IMAGE,conn);
	LOGI("---Java_com_vmc_ipc_ftp_FTPClient_FtpGet1");
	env->ReleaseStringUTFChars(out,output);
	env->ReleaseStringUTFChars(path,pathStr);
	return result;
}

/*
 * Class:     com_vmc_ipc_ftp_FTPClient
 * Method:    FtpPut
 * Signature: (Ljava/lang/String;Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_com_vmc_ipc_ftp_FTPClient_FtpPut
  (JNIEnv *env, jobject, jstring in, jstring path, jint mode){
	LOGI("---Java_com_vmc_ipc_ftp_FTPClient_FtpPut");
	if(!conn){
		throwFtpException(env,"you do not connect to the host.");
	}
	const char *remotePath = env->GetStringUTFChars(path,NULL);
	const char *input = env->GetStringUTFChars(in,NULL);
	int result = FtpPut(input,remotePath,mode,conn);
	env->ReleaseStringUTFChars(in,input);
	env->ReleaseStringUTFChars(path,remotePath);
	return result;
}
/*
 * Class:     com_vmc_ipc_ftp_FTPClient
 * Method:    FtpDelete
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_vmc_ipc_ftp_FTPClient_FtpDelete
  (JNIEnv *env, jobject, jstring path){
	LOGI("---Java_com_vmc_ipc_ftp_FTPClient_FtpDelete");
	if(!conn){
		throwFtpException(env,"you do not connect to the host.");
	}
	const char *remotePath = env->GetStringUTFChars(path,NULL);
	int result = FtpDelete(remotePath,conn);
	env->ReleaseStringUTFChars(path,remotePath);
	return result;
}

/*
 * Class:     com_vmc_ipc_ftp_FTPClient
 * Method:    FtpRename
 * Signature: (Ljava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_vmc_ipc_ftp_FTPClient_FtpRename
  (JNIEnv *env, jobject, jstring src, jstring dst){
	LOGI("---Java_com_vmc_ipc_ftp_FTPClient_FtpRename");
	if(!conn){
		throwFtpException(env,"you do not connect to the host.");
	}
	const char *srcFile = env->GetStringUTFChars(src,NULL);
	const char *dstFile = env->GetStringUTFChars(dst,NULL);
	int result = FtpRename(srcFile,dstFile,conn);
	env->ReleaseStringUTFChars(src,srcFile);
	env->ReleaseStringUTFChars(dst,dstFile);
	return result;
}

#ifdef __cplusplus
}
#endif


