#pragma once
#include <map>
#include <string>
#include "VNetTool.h"

namespace cmd
{
std::map<std::string,std::string> & StartServerStatusEnum();
void EndServerStatusEnum();

vmc::INetBase * GetCmdClient();

void AjustDeviceFps(double fps);
void SendDeviceInfo(const char *phoneType,const char *cpuinfo,const char *mac);
void SendRestartServer();
void SendRestartPublisher();
void SendRestartDevice();
void SendSetResolution(int width,int height);

void SendStartRecord(const char *sDir,const char *sName);
void SendStopRecord();
void SendSetBitrateControlType(int type);


void CommandConnectionStart();
int GetCmdProcessPoolId();

void CommandConnectionInit();
void CommandConnectionDestroy();

}

