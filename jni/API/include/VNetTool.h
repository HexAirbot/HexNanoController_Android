#pragma once
#include "rapidjson/document.h"

#define VNET_CMD_START "{\"cmd\":1}"
#define VNET_CMD_START_LEN 9


#define VNET_CMD_HEARTBEAT "{\"cmd\":2}"
#define VNET_CMD_HEARTBEAT_LEN 9


#define VNET_CMD_RESPOND 3


namespace vmc
{
class INetListener
{
public:
	virtual void OnDNSResolve(int err,bool bUdp){}
	//tcp only
	virtual void OnServerConneced(){}
	//tcp or udp
	virtual void OnClientConnected(bool bUdp){}
	virtual void OnDataReceived(void *p,int len,bool bUdp){}
	virtual void OnDisConnected(bool bUdp){}
};
class INetBase
{
public:
	virtual ~INetBase();
	virtual int SendAsyn(const void *p,int len,bool bUdp) = 0;
	virtual int SendAsyn(rapidjson::Document &d,bool bUdp,uint64_t *pRetSeq = NULL) = 0;
	//for client
	virtual int SetServerIP(const char *host,int port,bool bUdp) = 0;
	//for server
	virtual int StartServer(int port,bool bUdp) = 0;
	virtual void Close(bool bUdp) = 0;
	virtual void SetHeartBeatTimeout(int sec) = 0;
};



typedef int (*DATA_CALLBACK)(rapidjson::Document *);
typedef void (* ASYN_RESPOND_CALLBACK)(int ,uint64_t,int);
typedef void (* TIMER_CALLBACK)(int ,void *);
int RunIOLoop();
void ExitIOLoop();
int CreateThreadPool(int pool_id,int n);
int RegisterDataCallback(int cmd,int pool_id,DATA_CALLBACK callback);
int RegisterAsynRespondCallback(int cmd,ASYN_RESPOND_CALLBACK callback);
INetBase *CreateNetBase(INetListener *pLis);
void DestroyNetBase(INetBase *base);

void SetTimer(int timer_id,int pool_id,unsigned int msec,TIMER_CALLBACK callback,bool bRepeat,void *pData);
void KillTimer(int timer_id);


class NetSpeedCalc
{
private:
	uint64_t m_tLast;//ms
	double m_speedLast;
	double m_d;
	int m_frames;
	uint64_t m_bytes;
	int m_test;
	bool m_begin;
	int m_diff;
public:
	NetSpeedCalc(double d);
	void Reset();
	void Update(int bytes);
	double Speed();
};



}
