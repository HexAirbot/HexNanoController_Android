#pragma once
#include <jni.h>
namespace javatool
{
void InitEnv(JNIEnv *env);
void InitVM(JavaVM *vm);
void InitProxyObj(jobject objIpcProxy);

void InitJavaMethods();

void JavaShowStatus(const char *s,int state);
void JavaSendMsgByJni(int cmd,const char *msg);

}

