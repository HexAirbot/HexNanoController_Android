#pragma once
namespace debug
{
void SendDebugString();
void UpdateDebugInfo(const char *key,const char *value);
void UpdateDebugInfo(const char *key,double value);
void UpdateDebugInfo(const char *key,int value);
int GetDebugTimerId();
void StartDebugTimer();
void EnableDebug(bool bEnable);
}
