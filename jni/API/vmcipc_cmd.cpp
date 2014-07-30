#include <pthread.h>

#include "VNetTool.h"
#include "VCmdDefines.h"

#include "vmcipc_cmd.h"
#include "vmcipc_javatool.h"
#include "vmcipc_videostream.h"
#include "vmcipc_debug.h"

namespace cmd
{
	//创建客户端
static vmc::INetBase *g_client = NULL;
void CommandConnectionStart(int ,void *);
static pthread_mutex_t g_mutex_client = PTHREAD_MUTEX_INITIALIZER;

vmc::INetBase * GetCmdClient()
{
	return g_client;
}

static int SendCmdToDevAsyn(rapidjson::Document &doc,bool bUdp,uint64_t *pSeq = NULL)
{
	int ret = 0;
	pthread_mutex_lock(&g_mutex_client);
	if(!GetCmdClient())
	{
		ret = 1;
		goto end;
	}
	GetCmdClient()->SendAsyn(doc,bUdp,pSeq);
end:	
	pthread_mutex_unlock(&g_mutex_client);
	return ret;
}
static int SendCmdToDevAsyn(const void *p,int len,bool bUdp)
{
	int ret = 0;
	pthread_mutex_lock(&g_mutex_client);
	if(!GetCmdClient())
	{
		ret = 1;
		goto end;
	}
	GetCmdClient()->SendAsyn(p,len,bUdp);
end:	
	pthread_mutex_unlock(&g_mutex_client);
	return ret;
}

void AjustDeviceFps(double fps)
{
	rapidjson::Document doc;
	char buf[50];
	int ret = 0;
	doc.SetObject();
	doc.AddMember("cmd",(uint64_t)CMD_AJUST_FPS,doc.GetAllocator());
	sprintf(buf,"%d",(int)fps);
	doc.AddMember("fps",buf,doc.GetAllocator());
	SendCmdToDevAsyn(doc,true);
end:
	LOGW("%s ajust fps %d send,ret:%d",__FUNCTION__,(int)fps,ret);
	return ;
}
void SendDeviceInfo(const char *phoneType,const char *cpuinfo,const char *mac)
{
	rapidjson::Document doc;
	int ret = 0;
	doc.SetObject();
	doc.AddMember("cmd",(uint64_t)CMD_PHONE_INFO,doc.GetAllocator());
	doc.AddMember("phoneType",phoneType,doc.GetAllocator());
	doc.AddMember("cpuinfo",cpuinfo,doc.GetAllocator());
	doc.AddMember("mac",mac,doc.GetAllocator());
	SendCmdToDevAsyn(doc,true);
end:
	LOGW("%s ret:%d",__FUNCTION__,ret);
	return ;
}
void SendRestartServer()
{
	rapidjson::Document doc;
	int ret = 0;
	doc.SetObject();
	doc.AddMember("cmd",(uint64_t)CMD_RESTART_SERVER,doc.GetAllocator());
	SendCmdToDevAsyn(doc,true);
end:
	LOGW("%s ret:%d",__FUNCTION__,ret);
	return ;
}
void SendRestartPublisher()
{
	rapidjson::Document doc;
	int ret = 0;
	doc.SetObject();
	doc.AddMember("cmd",(uint64_t)CMD_RESTART_PUBLISHER,doc.GetAllocator());
	SendCmdToDevAsyn(doc,true);
end:
	LOGW("%s ret:%d",__FUNCTION__,ret);
	return ;
}
void SendRestartDevice()
{
	rapidjson::Document doc;
	int ret = 0;
	doc.SetObject();
	doc.AddMember("cmd",(uint64_t)CMD_RESTART_DEVICE,doc.GetAllocator());
	SendCmdToDevAsyn(doc,true);
end:
	LOGW("%s ret:%d",__FUNCTION__,ret);
	return ;
}
void SendSetResolution(int width,int height)
{
	rapidjson::Document doc;
	int ret = 0;
	rapidjson::Value v;
	char buf[100];
	doc.SetObject();
	doc.AddMember("cmd",(uint64_t)CMD_SET_RESOLUTION,doc.GetAllocator());
	
	sprintf(buf,"%d",width);
	v.SetString(buf,doc.GetAllocator());
	doc.AddMember("width",v,doc.GetAllocator());
	
	sprintf(buf,"%d",height);
	v.SetString(buf,doc.GetAllocator());
	doc.AddMember("height",v,doc.GetAllocator());
	
	SendCmdToDevAsyn(doc,true);
end:
	LOGW("%s ret:%d",__FUNCTION__,ret);
	return ;
}

void SendStartRecord(const char *sDir,const char *sName)
{
	rapidjson::Document doc;
	int ret = 0;
	rapidjson::Value v;
	char buf[200];
	doc.SetObject();
	doc.AddMember("cmd",(uint64_t)CMD_START_RECORD,doc.GetAllocator());

	
	sprintf(buf,"%s/%s",sDir,sName);
	v.SetString(buf,doc.GetAllocator());
	doc.AddMember("filename",v,doc.GetAllocator());
	
	SendCmdToDevAsyn(doc,true);
end:
	LOGW("%s ret:%d",__FUNCTION__,ret);
	return ;
}
void SendStopRecord()
{
	rapidjson::Document doc;
	int ret = 0;
	rapidjson::Value v;
	char buf[100];
	doc.SetObject();
	doc.AddMember("cmd",(uint64_t)CMD_STOP_RECORD,doc.GetAllocator());
	
	
	SendCmdToDevAsyn(doc,true);
end:
	LOGW("%s ret:%d",__FUNCTION__,ret);
	return ;
}

void SendSetBitrateControlType(int type)
{
	rapidjson::Document doc;
	int ret = 0;
	rapidjson::Value v;
	char buf[100];
	doc.SetObject();
	doc.AddMember("cmd",(uint64_t)CMD_SET_BPS_CTL_TYPE,doc.GetAllocator());
	
	sprintf(buf,"%d",type);
	v.SetString(buf,doc.GetAllocator());
	doc.AddMember("control_type",v,doc.GetAllocator());
	uint64_t seq;
	SendCmdToDevAsyn(doc,false,&seq);
	LOGW("%s seq:%llu",__FUNCTION__,seq);
	return ;
}


void SendSyncRemoteTime()
{
	time_t the_time;
	struct tm *tm_ptr;
	char bufTime[256];
	time(&the_time);
	tm_ptr=localtime(&the_time);
	strftime(bufTime,256,"%m%d%H%M%Y.%S",tm_ptr);
	time_t t =  time(NULL);
	sprintf(bufTime,"%zu",t);


	rapidjson::Document doc;
	int ret = 0;
	rapidjson::Value v;
	//char buf[100];
	doc.SetObject();
	doc.AddMember("cmd",(uint64_t)CMD_AJUST_REMOTE_TIME,doc.GetAllocator());
	
	//sprintf(buf,"%d",type);
	v.SetString(bufTime,doc.GetAllocator());
	doc.AddMember("time",v,doc.GetAllocator());



	struct tm lt = {0};
	localtime_r(&t, &lt);
	int zone = (int)lt.tm_gmtoff/3600;
	sprintf(bufTime,"UTC%s%d",(zone < 0 ? "+":"-"),(zone <  0 ? -zone:zone));
	v.SetString(bufTime,doc.GetAllocator());
	doc.AddMember("timezone",v,doc.GetAllocator());


	//uint64_t seq;
	SendCmdToDevAsyn(doc,true);
	LOGW("%s",__FUNCTION__);
	return ;
}




static pthread_t g_tidConnection = 0;
static bool g_bServerConnected = false;
static pthread_mutex_t g_mutex_ctl_data = PTHREAD_MUTEX_INITIALIZER;

void CommandConnectionStart();

//监控同服务器的连接状况
class ClientListener : public vmc::INetListener
{
public:
	void OnDNSResolve(int err,bool bUdp)
	{
		if(err)
		{
			LOGW("%s err bUdp:%d\n",__FUNCTION__,bUdp);
		}
		else
		if(bUdp)
		{
			SendCmdToDevAsyn(VNET_CMD_START,VNET_CMD_START_LEN,true);
		}
	}
	void OnServerConneced()
	{
		javatool::JavaShowStatus("connect success",1);
		g_bServerConnected =  true;
		debug::UpdateDebugInfo("connect",1);
		LOGW("%s\n",__FUNCTION__);
		javatool::JavaSendMsgByJni(MESSAGE_CONNECT_QUALITY_CHANGEGOOD,"");
		
	}
	void OnDataReceived(void *p,int len,bool bUdp)
	{
		//LOGW("%s\n",__FUNCTION__);
	}
	void OnDisConnected(bool bUdp)
	{
	
		javatool::JavaShowStatus("connect failed",0);
		g_bServerConnected = false;
		debug::UpdateDebugInfo("connect",0);
		LOGW("%s bUdp:%d\n",__FUNCTION__,bUdp);
		g_client->Close(!bUdp);
		javatool::JavaSendMsgByJni(MESSAGE_CONNECT_QUALITY_CHANGEBAD,"");
		//sleep(1);
		//if(!bUdp)
		{
		//CommandConnectionStart();
		vmc::SetTimer(TIMER_ID_RECONNECT,GetCmdProcessPoolId(),2400,CommandConnectionStart,false,NULL);
		}
	}
};

static ClientListener g_lisClient;
//static rapidjson::Document g_serverStatus;
static std::map<std::string,std::string> g_serverStatus;

static bool g_bCommandConnStarted = false;
static int ReadRecordState(rapidjson::Document *d);

//读取服务器推送的电量
static int ReadDataBattery(rapidjson::Document *d)
{
	pthread_mutex_lock(&g_mutex_ctl_data);
	//LOGW("%s battery:%d\n",__FUNCTION__,(*d)["battery"].GetInt());
	//LOGW("%s battery:%s\n",__FUNCTION__,(*d)["battery"].GetString());
	g_serverStatus["battery"] = (*d)["battery"].GetString();
	//g_serverStatus.AddMember("battery",(uint64_t)(*d)["battery"].GetInt(),g_serverStatus.GetAllocator());
	pthread_mutex_unlock(&g_mutex_ctl_data);
	return 0;
}

static int ReadDeviceDebugInfo(rapidjson::Document *d)
{
	LOGW("%s \n",__FUNCTION__);
#if 0	
	debug::UpdateDebugInfo("BandRatio",(*d)["rate"].GetString());
	debug::UpdateDebugInfo("Speed",(*d)["speed"].GetString());
#endif
	for (rapidjson::Value::ConstMemberIterator itr = d->MemberBegin(); itr != d->MemberEnd(); ++itr)
	{
		
		static const char* kTypeNames[] = { "Null", "False", "True", "Object", "Array", "String", "Number" };
		if(itr->value.GetType() == rapidjson::kStringType)
		{
			debug::UpdateDebugInfo(itr->name.GetString(),/*kTypeNames[itr->value.GetType()]*/itr->value.GetString());
		}
		
	}

	return 0;
}


//读取服务器推送的位置信息
static int ReadDataPos(rapidjson::Document *d)
{
	printf("%s x:%d y:%d\n",__FUNCTION__,(*d)["x"].GetInt(),(*d)["y"].GetInt());
	return 0;
}
static int ReadDeviceInfo(rapidjson::Document *d)
{
	//printf("%s x:%d y:%d\n",__FUNCTION__,(*d)["x"].GetInt(),(*d)["y"].GetInt());

	if(d->HasMember("width"))
	{
		printf("%s width:%s height:%s\n",__FUNCTION__,(*d)["width"].GetString(),(*d)["height"].GetString());
		char buf[500];
		sprintf(buf,"%s,%s",(*d)["width"].GetString(),(*d)["height"].GetString());
		javatool::JavaSendMsgByJni(MESSAGE_RESOLUTION_CHANGED,buf);
	}
	if(d->HasMember("remote_record_state"))
	{
		ReadRecordState(d);
	}
	SendSyncRemoteTime();
	
	return 0;
	
}
int ReadRecordState(rapidjson::Document *d)
{
	int ret = 0;
	pthread_mutex_lock(&g_mutex_ctl_data);
	if(d->HasMember("remote_record_state"))
	{
		//  状态值参照vmcipc_defines.h从STATE_RECORD_STOPED到ERR_RECORD
		g_serverStatus["remote_record_state"] = (*d)["remote_record_state"].GetString();
	}
	pthread_mutex_unlock(&g_mutex_ctl_data);
	
	
end:
	if(ret)
	{
		
	}
	
	return ret;
}

static void ReadResultSetBitrateControlType(int cmd,uint64_t seq,int result)
{
	LOGW("%s cmd:%d,seq:%llu,result:%d\n",__FUNCTION__,cmd,seq,result);
}

static void * CommandConnectionThread(void *)
{
	
	vmc::RunIOLoop();
	
	pthread_mutex_lock(&g_mutex_client);
	vmc::DestroyNetBase(g_client);
	g_client = NULL;
	pthread_mutex_unlock(&g_mutex_client);
	LOGW("%s end",__FUNCTION__);
	return NULL;
}
void CommandConnectionStart()
{
	LOGW("%s\n",__FUNCTION__);
	char ip[50];
	char *server = videostream::GetServerAddr();
	char *p = strstr(server,"rtmp://");
	char *p0 = server;
	if(p)
	{
		p += 7;
		p0 = p;
		p = strstr(p,"/");
	}
	if(!p)
	{
		p += strlen(p0);
	}
	memset(ip,0,sizeof(ip));
	memcpy(ip,p0,p - p0);
	LOGW("%s ip:%s\n",__FUNCTION__,ip);
	g_client->SetHeartBeatTimeout(4);
	//udp resolve dns start
	g_client->SetServerIP(ip,PORT_COMMAND_SERVER_UDP,true);
	//tcp resolve dns start and connect
	g_client->SetServerIP(ip,PORT_COMMAND_SERVER_TCP,false);
}
void CommandConnectionStart(int ,void *)
{
	CommandConnectionStart();
}
int GetCmdProcessPoolId()
{
	return POOL_ID_COMMAND;
}


void CommandConnectionInit()
{
	pthread_attr_t attr;
	if(g_bCommandConnStarted)
	{
		goto end;
	}
	g_bCommandConnStarted = true;
	//g_serverStatus.SetObject();
	vmc::CreateThreadPool(GetCmdProcessPoolId(),2);
	
	//注册数据处理函数	
	vmc::RegisterDataCallback(CMD_REPORT_BATTERY,GetCmdProcessPoolId(),ReadDataBattery);
	vmc::RegisterDataCallback(CMD_REPORT_POSITION,GetCmdProcessPoolId(),ReadDataPos);
	vmc::RegisterDataCallback(CMD_REPORT_DEVICE_INFO,GetCmdProcessPoolId(),ReadDeviceInfo);
	vmc::RegisterDataCallback(CMD_REPORT_RECORD_ERROR,GetCmdProcessPoolId(),ReadRecordState);
	vmc::RegisterDataCallback(CMD_REPORT_DEVICE_DEBUG_INFO,GetCmdProcessPoolId(),ReadDeviceDebugInfo);
	vmc::RegisterAsynRespondCallback(CMD_SET_BPS_CTL_TYPE,ReadResultSetBitrateControlType);

	
	g_client = vmc::CreateNetBase(&g_lisClient);
	CommandConnectionStart();
	pthread_attr_init(&attr);
	pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);
	pthread_create(&g_tidConnection,&attr,CommandConnectionThread,NULL);
end:
	return ;
}
void CommandConnectionDestroy()
{
	void *result = NULL;
	vmc::ExitIOLoop();
	if(g_tidConnection != 0)
	{
		LOGW("%s g_tidConnection:%d\n",__FUNCTION__,g_tidConnection);
		pthread_join(g_tidConnection,&result);
		g_tidConnection = 0;
	}
}
std::map<std::string,std::string> & StartServerStatusEnum()
{
	pthread_mutex_lock(&g_mutex_ctl_data);
	return g_serverStatus;
}
void EndServerStatusEnum()
{
	pthread_mutex_unlock(&g_mutex_ctl_data);
}
}

