#include <string>
#include <android/native_window.h> // requires ndk r5 or newer
#include <android/native_window_jni.h> // requires ndk r5 or newer

#include "VCmdDefines.h"
#include "vmcipc_videostream.h"
#include "vmcipc_cmd.h"
#include "vmcipc_javatool.h"
#include "vmcipc_debug.h"

namespace debug
{

static std::map<std::string,std::string> g_mapDebug;
static pthread_mutex_t g_mutex_debug = PTHREAD_MUTEX_INITIALIZER;

void SendDebugString()
{
	std::map<std::string,std::string>::iterator iter;
	std::string str;
	str = "";
	pthread_mutex_lock(&g_mutex_debug);
	for(iter = g_mapDebug.begin();iter != g_mapDebug.end();iter++)
	{
		str += iter->first;
		str += ":";
		str += iter->second;
		str += "\r\n";
	}
	pthread_mutex_unlock(&g_mutex_debug);
	if(str.size() > 0)
	{
		javatool::JavaSendMsgByJni(MESSAGE_REFRESH_DEBUG,str.c_str());
	}
	
}
void UpdateDebugInfo(const char *key,const char *value)
{
	pthread_mutex_lock(&g_mutex_debug);
	g_mapDebug[key] = value;
	pthread_mutex_unlock(&g_mutex_debug);
}
void UpdateDebugInfo(const char *key,double value)
{
	char buf[100];
	sprintf(buf,"%.2f",(float)value);
	UpdateDebugInfo(key,buf);
}
void UpdateDebugInfo(const char *key,int value)
{
	char buf[100];
	sprintf(buf,"%d",value);
	UpdateDebugInfo(key,buf);
}
int GetDebugTimerId()
{
	return TIMER_ID_DEBUG;
}
static void DebugTimerCallback(int ,void *)
{
	SendDebugString();
}
void StartDebugTimer()
{
	vmc::SetTimer(GetDebugTimerId(),cmd::GetCmdProcessPoolId(),1000,DebugTimerCallback,true,NULL);
}
}

